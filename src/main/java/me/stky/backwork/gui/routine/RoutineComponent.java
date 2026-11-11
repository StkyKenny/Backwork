package me.stky.backwork.gui.routine;

import lombok.Getter;
import me.stky.backwork.gui.MainFrame;
import me.stky.backwork.gui.task.ScrollableToolTip;
import me.stky.backwork.model.routine.Routine;
import me.stky.backwork.service.Helper;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.function.Consumer;

public class RoutineComponent extends JPanel {

    @Getter
    private Routine routine;
    private final MainFrame mainframe;
    private final RoutineListener listener;
    private final String tooltipDescription;
    MouseAdapter hoverListener;
    private boolean isHovered = false;
    private boolean isSelected = false;

    private JCheckBox checkBox;

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private Font defaultFont;

    /**
     * Constructor for RoutineComponent
     *
     * @param mainframe
     * @param listener
     * @param routine
     * @param sideEffect Consume self, for additional instructions that aren't linked to this component
     */
    public RoutineComponent(MainFrame mainframe, RoutineListener listener, Routine routine, Consumer<RoutineComponent> sideEffect) {
        this.mainframe = mainframe;
        this.listener = listener;
        this.routine = routine;
        tooltipDescription = "<html><div>" + Helper.prettyPrintUsingGson(routine)
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\n", "<br>")
                .replace(" ", "&nbsp;")
                + "</div></html>";
        hoverListener = new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent me) {
                // Assuming a timer mechanism similar to TaskComponent exists or is handled by the parent
                if (!isHovered) {
                    isHovered = true;
                    setToolTipText(tooltipDescription);
                    checkBox.setToolTipText(tooltipDescription);
                    setBackground(new Color(245, 245, 245));
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (!isSelected) {
                    // Timer logic would go here if needed
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
        return new ScrollableToolTip(); // Reusing ScrollableToolTip
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
            setBackground(new Color(220, 220, 220));
        } else {
            setBackground(Color.WHITE);
        }
        listener.onSelectRoutine(this);
        isSelected = !isSelected;
    }

    /**
     * @param sideEffect Consume self, for additional instructions that aren't linked to this component
     */
    private void init(Consumer<RoutineComponent> sideEffect) {
        setLayout(new GridBagLayout());
        var routineName = routine.getName();

        var lowerInfo = "";
        if (routine.getLastExecuted() != null) {
            lowerInfo = dateFormat.format(Date.from(routine.getLastExecuted()));
        }
        var intervalInfo = routine.getInterval() != null ? " | Each " + routine.getInterval() + " " + routine.getIntervalUnit() : "";

        // Check if it's scheduled to run soon
        String nextRunInfo = routine.getNextScheduledDate() != null ? " (Next: " + dateFormat.format(Date.from(routine.getNextScheduledDate())) + ")" : "";

        var routineNameDisplay = routineName;
        lowerInfo += " " + intervalInfo + nextRunInfo;

        checkBox = new JCheckBox(routineNameDisplay) {
            @Override
            public JToolTip createToolTip() {
                return new ScrollableToolTip();
            }
        };
        checkBox.setFont(checkBox.getFont().deriveFont(Font.BOLD, 18f));
        FontMetrics fm = checkBox.getFontMetrics(checkBox.getFont());
        checkBox.setText(ellipsize(routineNameDisplay, fm, this.mainframe.getWidth() - 200));
        checkBox.setMaximumSize(new Dimension(50, Integer.MAX_VALUE));
        defaultFont = checkBox.getFont();
        checkBox.setOpaque(false);
        updateGUIGivenStatus();

        checkBox.addActionListener(actionEvent -> {
            if (selectModifierEvent(actionEvent)) {
                checkBox.setSelected(false);
                return;
            }
            // Routine completion logic would go here
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
        add(new JLabel("?/?"), setGbc(gbc, 2, posY, 0, 1));
        posY += 1;
        add(dateLabel, setGbc(gbc, 0, posY, 2, 1));
        add(moreActionsButton, setGbc(gbc, 2, posY, 0, 1));
        posY += 1;

        // Description area (simplified, as Routine doesn't have a description field in the same way Task does, but we keep the structure)
        if (!routine.getDescription().isEmpty()) {
            JTextArea textArea = new JTextArea(4, 20);
            textArea.setText(routine.getDescription());
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
            // Mouse wheel listener logic omitted for brevity but should be included
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

    private GridBagConstraints setGbc(GridBagConstraints gbc, int posX, int posY, int weight, int widthX) {
        gbc.gridx = posX;
        gbc.gridy = posY;
        gbc.weightx = weight;
        gbc.gridwidth = widthX;
        return gbc;
    }

    /**
     * Setter for the routine field
     */
    public void updateRoutine(Routine routine) {
        this.routine = routine;
        updateGUIGivenStatus();
    }

    /**
     * Method used to force UI update
     */
    public void updateGUIGivenStatus() {
        switch (!routine.wasExecuted()) {
            case true -> {
                checkBox.setFont(defaultFont);
                checkBox.setEnabled(true);
                checkBox.setSelected(false);
                // Custom coloring based on routine state
                checkBox.setForeground(new Color(60, 110, 210));
            }
            case false -> {
                checkBox.setEnabled(false);
                checkBox.setForeground(new Color(150, 99, 235));
            }
        }
    }

    private void showChoices(JButton button) {
        JPopupMenu menu = new JPopupMenu();

        JMenuItem choice3 = new JMenuItem("Edit Routine");
        JMenuItem choice1 = new JMenuItem("Archive Routine");
        JMenuItem choice2 = new JMenuItem("Delete Routine");

        choice3.addActionListener(_ -> listener.onEditRoutine(this));
        choice1.addActionListener(_ -> listener.onArchiveRoutine(this));
        choice2.addActionListener(_ -> listener.onDeleteRoutine(this));

        menu.add(choice3);
        menu.add(choice1);
        menu.add(choice2);

        menu.show(button, 0, button.getHeight());
    }
}