package me.stky.backwork.gui;

import lombok.Getter;
import me.stky.backwork.model.Task;
import me.stky.backwork.service.Helper;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
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

    private JCheckBox checkBox;
    private final int height = 60;

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
        init(sideEffect);
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent me) {
                setToolTipText("<html><p>" + Helper.prettyPrintUsingGson(task)
                        .replace("&", "&amp;")
                        .replace("<", "&lt;")
                        .replace(">", "&gt;")
                        .replace("\n", "<br>")
                        .replace(" ", "&nbsp;")
                        + "</p></html>");
            }
        });
    }

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
     * @param sideEffect Consume self, for additional instructions that aren't linked to this component
     */
    private void init(Consumer<TaskComponent> sideEffect) {
        //setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setLayout(new GridBagLayout());
        var taskName = task.getName();

        var lowerInfo = dateFormat.format(Date.from(task.getCreatedDate()));
        var recurTaskInfo = task.getRecurringTaskInfo();

        if (recurTaskInfo != null) {
            taskName = "🔁 " + task.getName();
            lowerInfo += " | Each " + recurTaskInfo.getInterval() + " " + recurTaskInfo.getIntervalUnit();
        }
        try {
            lowerInfo += " | " + task.displayTimeRemaining();
        } catch (IllegalStateException _) {

        }

        checkBox = new JCheckBox(taskName);
        checkBox.setFont(checkBox.getFont().deriveFont(Font.BOLD, 18f));
        FontMetrics fm = checkBox.getFontMetrics(checkBox.getFont());
        checkBox.setText(ellipsize(taskName, fm, this.mainframe.getWidth() - 200));
        checkBox.setMaximumSize(new Dimension(50, Integer.MAX_VALUE));
        defaultFont = checkBox.getFont();
        updateGUIGivenStatus();

        checkBox.addActionListener(_ -> {
            this.mainframe.playSound("src/main/resources/sfx/bloopsfx_cut.wav");
            updateGUIGivenStatus();
            sideEffect.accept(this);
        });

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

            JScrollPane scrollPane = new JScrollPane(
                    textArea,
                    JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                    JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
            );
            scrollPane.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
            scrollPane.setOpaque(false);
            scrollPane.getViewport().setOpaque(false);
            textArea.setMargin(new Insets(4, 4, 4, 4));

            textArea.setCaretPosition(0);
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

        this.task.getRecurringTaskInfo().getMissedDaysCount();
        JMenuItem choice3 = new JMenuItem("Edit Task");
        JMenuItem choice1 = new JMenuItem("Archive Task");
        JMenuItem choice2 = new JMenuItem("Delete Task");
        JMenuItem choicetest = new JMenuItem("Test");

        choice3.addActionListener(_ -> listener.onEditTask(this));
        choice1.addActionListener(_ -> listener.onArchiveTask(this));
        choice2.addActionListener(_ -> listener.onDelete(this));
        choicetest.addActionListener(_ -> this.task.getRecurringTaskInfo().computeNextDate());

        menu.add(choice3);
        menu.add(choice1);
        menu.add(choice2);
        menu.add(choicetest);

        menu.show(button, 0, button.getHeight());
    }
}
