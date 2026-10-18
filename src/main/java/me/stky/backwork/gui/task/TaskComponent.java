package me.stky.backwork.gui.task;

import lombok.Getter;
import me.stky.backwork.gui.MainFrame;
import me.stky.backwork.model.Task;
import me.stky.backwork.service.Helper;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.font.TextAttribute;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.function.Consumer;

public class TaskComponent extends JPanel {

    @Getter
    private Task task;
    private final MainFrame mainframe;
    private final TaskListener listener;
    private String tooltipDescription;
    MouseAdapter hoverListener;
    private boolean isHovered = false;
    private boolean isSelected = false;

    private JCheckBox checkBox;

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private Font defaultFont;

    /**
     * Constructor for TaskComponent
     *
     * @param mainframe
     * @param task
     * @param sideEffect Consume self, for additional instructions that aren't linked to this component
     */
    public TaskComponent(MainFrame mainframe, TaskListener listener, Task task, Consumer<TaskComponent> sideEffect) {
        this.mainframe = mainframe;
        this.listener = listener;
        this.task = task;
        tooltipDescription = "<html><div>" + Helper.prettyPrintUsingGson(task)
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\n", "<br>")
                .replace(" ", "&nbsp;")
                + "</div></html>";
        hoverListener = new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent me) {
                hoverExitTimer.stop();
                if (!isHovered) {
                    isHovered = true;
                    setToolTipText(tooltipDescription);
                    checkBox.setToolTipText(tooltipDescription);
                    setBackground(new Color(245, 245, 245));
                    //mainframe.playSound("src/main/resources/sfx/hoverIn.wav");
                }
            }


            @Override
            public void mouseExited(MouseEvent e) {
                if (!isSelected) {
                    hoverExitTimer.restart();
                }
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.isControlDown() || e.isAltDown()) {
                    toggleSelected();
                }
            }
        };
        addMouseListener(hoverListener);
        init(sideEffect);


    }

    @Override
    public JToolTip createToolTip() {
        return new ScrollableToolTip();
    }

    /**
     * This minor delay is used to check is mouse hover is going from inner panel to inner checkbox
     * Instead of IN (panel)-> OUT (panel)-> IN (checkbox) -> OUT(checkbox) : causing the sfx to trigger twice on this panel
     */
    private final Timer hoverExitTimer = new Timer(50, e -> {
        isHovered = false;
        setToolTipText(null);
        checkBox.setToolTipText(null);
        setBackground(new Color(238, 238, 238));
    });


    private static String ellipsize(String text, FontMetrics fm, int maxWidth) {
        if (fm.stringWidth(text) <= maxWidth) {
            return text;
        }

        String ellipsis = "...";
        int ellipsisWidth = fm.stringWidth(ellipsis);

        int end = text.length();

        while (end > 0 &&
                fm.stringWidth(text.substring(0, end)) + ellipsisWidth > maxWidth) {
            end--;
        }
        return text.substring(0, end) + ellipsis;
    }

    /**
     * Use to toggle selected when using [CTRL / ALT / SHIFT]  with click
     *
     * @param actionEvent
     * @return True is the event was caught with modifier
     */
    private boolean selectModifierEvent(ActionEvent actionEvent) {
        if (((actionEvent.getModifiers() & ActionEvent.CTRL_MASK) != 0) || ((actionEvent.getModifiers() & ActionEvent.ALT_MASK) != 0) || ((actionEvent.getModifiers() & ActionEvent.SHIFT_MASK) != 0)) {
            toggleSelected();
            return true;
        }
        return false;
    }

    private boolean selectModifierEvent(MouseEvent e) {
        if (e.isControlDown() || e.isAltDown() || e.isShiftDown()) {
            toggleSelected();
            e.consume();
            return true;
        }
        return false;
    }

    public void toggleSelected() {
        if (isSelected) {
            // unselect
            setBackground(new Color(220, 220, 220));
        } else {
            // select
            setBackground(Color.WHITE);
        }
        listener.onSelectTask(this);
        isSelected = !isSelected;
    }

    /**
     * @param sideEffect Consume self, for additional instructions that aren't linked to this component
     */
    private void init(Consumer<TaskComponent> sideEffect) {
        //setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setLayout(new GridBagLayout());
        var taskName = task.getName();

        var lowerInfo = dateFormat.format(Date.from(task.getCreatedDate()));
        var recurTaskInfo = task.getRecurringTaskInfo();
        var deadlineIndicator = new StringBuilder();
        if (recurTaskInfo != null) {
            deadlineIndicator.append("🔁");
        }
        if (task.getDeadline() != null) {
            deadlineIndicator.append("⏳");
        }
        taskName = deadlineIndicator.toString() + " " + task.getName();


        if (recurTaskInfo != null) {
            lowerInfo += " | Each " + recurTaskInfo.getInterval() + " " + recurTaskInfo.getIntervalUnit();
        }
        try {
            lowerInfo += " | " + task.displayTimeRemaining();
        } catch (IllegalStateException _) {

        }

        checkBox = new JCheckBox(taskName) {
            @Override
            public JToolTip createToolTip() {
                return new ScrollableToolTip();
            }

        };
        checkBox.setFont(checkBox.getFont().deriveFont(Font.BOLD, 18f));
        FontMetrics fm = checkBox.getFontMetrics(checkBox.getFont());
        checkBox.setText(ellipsize(taskName, fm, this.mainframe.getWidth() - 200));
        checkBox.setMaximumSize(new Dimension(50, Integer.MAX_VALUE));
        defaultFont = checkBox.getFont();
        checkBox.setOpaque(false);
        updateGUIGivenStatus();

        checkBox.addActionListener(actionEvent -> {

            if (selectModifierEvent(actionEvent)) {
                checkBox.setSelected(false);
                return;
            }
            System.out.println("competed");
            this.mainframe.playSound("src/main/resources/sfx/bloopsfx_cut.wav");
            updateGUIGivenStatus();
            sideEffect.accept(this);
        });

        checkBox.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                selectModifierEvent(e);
            }
        });
        checkBox.addMouseListener(hoverListener);
        var dateLabel = new JLabel(lowerInfo);
        var moreActionsButton = new JButton("--");
        moreActionsButton.addActionListener(_ -> showChoices(moreActionsButton));

        GridBagConstraints gbc = createGridConstraints();

        int posY = 0;
        add(checkBox, setGbc(gbc, 0, posY, 1, 2));
        add(new JLabel("?/?"), setGbc(gbc, 2, posY, 0, 1)); // TODO : idk yet what i will add here
        posY += 1;
        add(dateLabel, setGbc(gbc, 0, posY, 2, 1));
        add(moreActionsButton, setGbc(gbc, 2, posY, 0, 1));
        posY += 1;
        if (!task.getDescription().isEmpty()) {
            JTextArea textArea = new JTextArea(4, 20);

            textArea.setText(task.getDescription());
            textArea.setLineWrap(true);
            textArea.setWrapStyleWord(true);
            textArea.setEditable(false);
            textArea.setOpaque(false);
            textArea.setBorder(null);
            textArea.setRows(5);

            JScrollPane scrollPane = new JScrollPane(
                    textArea,
                    JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                    JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
            );
            scrollPane.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
            scrollPane.setOpaque(false);
            scrollPane.getViewport().setOpaque(false);
            scrollPane.addMouseWheelListener(e -> {
                // This part was written with AI
                JScrollBar bar = scrollPane.getVerticalScrollBar();
                int rotation = e.getWheelRotation();
                boolean canScrollUp = bar.getValue() > bar.getMinimum();
                boolean canScrollDown = bar.getValue() < bar.getMaximum() - bar.getVisibleAmount();
                boolean innerCanScroll = (rotation < 0 && canScrollUp) || (rotation > 0 && canScrollDown);

                if (!innerCanScroll) {
                    JScrollPane parent = findParentScrollPane(scrollPane);

                    if (parent != null) {
                        MouseWheelEvent forwarded = (MouseWheelEvent) SwingUtilities.convertMouseEvent(scrollPane, e, parent);
                        parent.dispatchEvent(forwarded);
                    }
                }
            });
            textArea.setMargin(new Insets(4, 4, 4, 4));

            textArea.setCaretPosition(0);
            textArea.addMouseListener(hoverListener);
            add(scrollPane, setGbc(gbc, 0, posY, 2, 1));
        }
        posY += 1;

        revalidate();
        repaint();

    }

    private GridBagConstraints createGridConstraints() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 5, 1, 5); // margins
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        return gbc;
    }

    private GridBagConstraints setGbc(GridBagConstraints gbc, int posX, int posY, int weight) {
        return setGbc(gbc, posX, posY, weight, 1);

    }

    private GridBagConstraints setGbc(GridBagConstraints gbc, int posX, int posY, int weight, int widthX) {
        gbc.gridx = posX;
        gbc.gridy = posY;
        gbc.weightx = weight;
        gbc.gridwidth = widthX;
        return gbc;
    }

    /**
     * Setter for the task field
     */
    public void updateTask(Task task) {
        this.task = task;
        updateGUIGivenStatus();
    }

    /**
     * Method used to force UI update
     */
    public void updateGUIGivenStatus() {
        switch (task.getStatus()) {
            case TODO -> {
                checkBox.setFont(defaultFont);
                checkBox.setEnabled(true);
                checkBox.setSelected(false);
                if (task.getRecurringTaskInfo() != null) {
                    checkBox.setForeground(new Color(234, 88, 12));
                } else {
                    checkBox.setForeground(new Color(234, 234, 12));
                }
            }
            case IN_PROGRESS -> {
                checkBox.setForeground(new Color(37, 99, 235));
            }
            case COMPLETED -> {
                Font newFont = checkBox.getFont().deriveFont(
                        Map.of(TextAttribute.STRIKETHROUGH, TextAttribute.STRIKETHROUGH_ON)
                );
                checkBox.setForeground(new Color(100, 116, 139));
                checkBox.setFont(newFont);
                checkBox.setEnabled(false);
            }
            case ARCHIVED -> {
                checkBox.setForeground(new Color(124, 58, 237));
            }
        }
    }

    private void showChoices(JButton button) {
        JPopupMenu menu = new JPopupMenu();


        JMenuItem choice3 = new JMenuItem("Edit Task");
        JMenuItem choice1 = new JMenuItem("Archive Task");
        JMenuItem choice2 = new JMenuItem("Delete Task");
        JMenuItem choicetest = new JMenuItem("Test");

        choice3.addActionListener(_ -> listener.onEditTask(this));
        choice1.addActionListener(_ -> listener.onArchiveTask(this));
        choice2.addActionListener(_ -> listener.onDelete(this));
        choicetest.addActionListener(_ -> {
        });

        menu.add(choice3);
        menu.add(choice1);
        menu.add(choice2);
        menu.add(choicetest);

        menu.show(button, 0, button.getHeight());
    }


    /**
     * @param component
     * @return
     */

    private static JScrollPane findParentScrollPane(Component component) {
        Container parent = component.getParent();

        while (parent != null) {
            if (parent instanceof JScrollPane) {
                return (JScrollPane) parent;
            }

            parent = parent.getParent();
        }

        return null;
    }
}
