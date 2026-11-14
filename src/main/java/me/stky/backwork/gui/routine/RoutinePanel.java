package me.stky.backwork.gui.routine;

import com.toedter.calendar.JDateChooser;
import me.stky.backwork.gui.FileOrListSelectorPanel;
import me.stky.backwork.gui.MainFrame;
import me.stky.backwork.gui.task.ScrollableConfirmDialog;
import me.stky.backwork.model.IntervalUnit;
import me.stky.backwork.model.routine.Routine;
import me.stky.backwork.service.CustomExecutionsService;
import me.stky.backwork.service.Helper;
import me.stky.backwork.service.ReflectionUtils;
import me.stky.backwork.service.RoutineService;
import org.bson.BsonValue;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public class RoutinePanel extends JPanel implements RoutineListener {

    public JPanel routinePanel = new JPanel();
    public JPanel routineBasePanel = new JPanel();
    private final MainFrame mainframe;
    private final RoutineService routineService;
    private List<Routine> routines = new ArrayList<>();
    CustomExecutionsService customExecutionsService;

    private final List<RoutineComponent> routineComponents = new ArrayList<>();
    private final List<RoutineComponent> selectedRoutineComponents = new ArrayList<>();
    private final JToggleButton completedToggle = new JToggleButton("Toggle Completed ✕");

    public RoutinePanel(MainFrame mainframe, RoutineService routineService,
                        CustomExecutionsService customExecutionsService) {
        this.customExecutionsService = customExecutionsService;
        this.routineService = routineService;
        this.mainframe = mainframe;
        routinePanel.setLayout(new BoxLayout(routinePanel, BoxLayout.Y_AXIS));
        routineBasePanel.setLayout(new BoxLayout(routineBasePanel, BoxLayout.Y_AXIS));

        routinePanel.add(routineBasePanel);
        routinePanel.add(new JSeparator());

        JScrollPane scrollPane = new JScrollPane(routinePanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        JPanel buttonsPanel = new JPanel();
        var addRoutineButton = new JButton("Add Routine");
        addRoutineButton.addActionListener(_ -> addRoutine(this.openContextAddRoutine()));
        buttonsPanel.add(addRoutineButton);


        completedToggle.addActionListener(_ -> {
            if (completedToggle.isSelected()) {
                for (var routineComponent : routineComponents) {
                    routineComponent.setVisible(true);
                }
                completedToggle.setText("Toggle Completed ✓");
            } else {
                for (var routineComponent : routineComponents) {
                    if (routineComponent.getRoutine().wasExecuted()) {
                        routineComponent.setVisible(false);
                    }
                }
                completedToggle.setText("Toggle Completed ✕");
            }

            routinePanel.revalidate();
            routinePanel.repaint();
        });


        buttonsPanel.add(completedToggle);
        routinePanel.add(buttonsPanel);

        var refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(_ -> refreshRoutines());
        buttonsPanel.add(refreshButton);

        var batchOpsButton = new JButton("Batch ops");
        batchOpsButton.addActionListener(_ -> showBatchOperationsMenu(batchOpsButton));
        buttonsPanel.add(batchOpsButton);

        loadRoutines();

        // THIS IS IMPORTANT
        setLayout(new BorderLayout());
        add(buttonsPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
    }

    private GridBagConstraints createGridConstraints() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5); // margins
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        return gbc;
    }

    private GridBagConstraints setGbc(GridBagConstraints gbc, int posX, int posY, int weight) {
        gbc.gridx = posX;
        gbc.gridy = posY;
        gbc.weightx = weight;
        return gbc;
    }

    /**
     * Wrapper of openContextAddRoutine(Optional<Routine> presetRoutine) for setting no preset values
     *
     * @return Optional Routine created
     */
    public Optional<Routine> openContextAddRoutine() {
        return openContextAddRoutine(Optional.empty());
    }

    /**
     * @param presetRoutine An Optional Routine used to set default values
     * @return Optional Routine created
     */
    public Optional<Routine> openContextAddRoutine(Optional<Routine> presetRoutine) {

        Routine defaultValuesRoutine = new Routine(null, Helper.rdnName(), "", "", null, null, 1, IntervalUnit.DAYS);
        if (presetRoutine.isPresent()) {
            defaultValuesRoutine = presetRoutine.get();
        }

        // --- Dialog creation logic (similar to TaskPanel) ---
        JPanel panel2 = new JPanel(new GridBagLayout());
        panel2.setMinimumSize(new Dimension(750, 50));
        GridBagConstraints gbc = createGridConstraints();
        int rowIndex = 0;

        panel2.add(new JLabel("Name :"), gbc);
        JTextField nameField = new JTextField(defaultValuesRoutine.getName());
        panel2.add(nameField, setGbc(gbc, 1, rowIndex, 1));

        rowIndex += 1;
        panel2.add(new JLabel("Description :"), setGbc(gbc, 0, rowIndex, 0));
        JTextArea textArea = new JTextArea(4, 20);
        textArea.setText(defaultValuesRoutine.getDescription());
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(textArea);
        panel2.add(scrollPane, setGbc(gbc, 1, rowIndex, 1));

        rowIndex += 1;
        panel2.add(new JLabel("Target Executable Path:"), setGbc(gbc, 0, rowIndex, 0));
        FileOrListSelectorPanel pathField = new FileOrListSelectorPanel(
                customExecutionsService, ReflectionUtils.getAllDeclaredMethodNames(CustomExecutionsService.class));
        panel2.add(pathField, setGbc(gbc, 1, rowIndex, 1));


        rowIndex += 1;
        var startingDateLabel = new JLabel("Starting Date:");
        panel2.add(startingDateLabel, setGbc(gbc, 0, rowIndex, 0));
        JDateChooser calendar = new JDateChooser(Date.from(Instant.now()));
        calendar.setDateFormatString("yyyy-MM-dd");
        panel2.add(calendar);
        panel2.add(calendar, setGbc(gbc, 1, rowIndex, 1), 5);

        rowIndex += 1;
        panel2.add(new JLabel("Interval"), setGbc(gbc, 0, rowIndex, 0));
        JSpinner recurrentAmount = new JSpinner(new SpinnerNumberModel(1, 1, 9999, 1));
        panel2.add(recurrentAmount, setGbc(gbc, 1, rowIndex, 1));

        JLabel recurrentLabel = new JLabel("Unit:");
        final JComboBox<IntervalUnit> cb = new JComboBox<>(IntervalUnit.values());
        panel2.add(recurrentLabel, setGbc(gbc, 0, rowIndex + 1, 0));
        panel2.add(cb, setGbc(gbc, 1, rowIndex + 1, 1));


        int result = JOptionPane.showConfirmDialog(
                null,
                panel2,
                "Add new Routine",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (result == JOptionPane.OK_OPTION) {
            String name = nameField.getText();
            String description = textArea.getText();
            String path = pathField.getSelectedValue();
            int amount = (Integer) recurrentAmount.getValue();
            IntervalUnit unit = (IntervalUnit) cb.getSelectedItem();

            if (name == null || name.isBlank()) {
                JOptionPane.showMessageDialog(null, "Can't be untitled");
                return Optional.empty();
            }

            if (calendar.getDate() == null) {
                this.mainframe.appendMessage("Please enter or select a valid date");
                return Optional.empty();
            }

            Routine newRoutine = new Routine(null, name, description, path, null, calendar.getDate().toInstant(), amount, unit);
            return Optional.of(newRoutine);
        }
        return Optional.empty();
    }

    private void completeRoutine(Routine routine, RoutineComponent routineComponent) {
        try {
            routine = routine.completeOnce();
            Routine updatedRoutine = routineService.completeRoutine(routine);
            routineComponent.updateRoutine(updatedRoutine);
        } catch (SQLException e) {
            mainframe.appendMessage("There was a problem with DB trying to complete the routine :" + routine.getName());
            return;
        }
        if (!completedToggle.isSelected()) {
            routineComponent.setVisible(false);
        }
        routinePanel.revalidate();
        routinePanel.repaint();
    }

    private void addRoutine(Optional<Routine> routineOpt) {
        if (routineOpt.isEmpty()) {
            return;
        }
        Routine routine = routineOpt.get();
        BsonValue upsertedID = null;
        try {
            // Assuming RoutineService has an addTask method
            routine = routineService.addTask(routine);
            routines.add(routine); // add in current memory list
        } catch (SQLException e) {
            mainframe.appendMessage("---------------------------->");
            mainframe.appendMessage("Error trying to add this new Routine : " + routine);
        }
        mainframe.appendMessageDEBUG("---------------------------->");
        mainframe.appendMessageDEBUG("New routine added : " + routine);
        mainframe.appendMessageDEBUG("with ID : " + upsertedID);
        addRoutineInGUI(routine); // GUI
    }

    private void addRoutineInGUI(Routine routine) {
        RoutineComponent routineComponent = new RoutineComponent(this.mainframe, this, routine,
                component -> {
                    completeRoutine(routine, component);
                    mainframe.appendMessageDEBUG("Completed : " + routine);
                });

        if (!completedToggle.isSelected() && routine.wasExecuted()) {
            routineComponent.setVisible(false);
            routinePanel.revalidate();
            routinePanel.repaint();
        }
        routineComponents.add(routineComponent);
        routineBasePanel.add(routineComponent);

        routinePanel.revalidate();
        routinePanel.repaint();
    }

    /**
     * It will check and update recurring routines clearing them
     */
    public void refreshRoutines() {
        clearRoutinesFromPanel();
        loadRoutines();
    }

    private void showBatchOperationsMenu(JButton button) {
        JPopupMenu menu = new JPopupMenu();

        JMenuItem choice2 = new JMenuItem("Delete Routine");
        JMenuItem choice1 = new JMenuItem("Archive Routine");
        JMenuItem choicetest = new JMenuItem("Test");

        choice2.addActionListener(_ -> batchRemoveRoutineComponent(selectedRoutineComponents));
        choice1.addActionListener(_ -> batchArchiveRoutineComponent(selectedRoutineComponents));
        choicetest.addActionListener(_ -> mainframe.appendMessage("Selected : " + selectedRoutineComponents.size() + " routines."));

        menu.add(choice2);
        menu.add(choice1);
        menu.add(choicetest);

        menu.show(button, 0, button.getHeight());
    }

    public void batchArchiveRoutineComponent(List<RoutineComponent> routineComponents) {

        int result = ScrollableConfirmDialog.show(
                null,
                "Are you sure you want to archive all : " + routineComponents.size() + " selected routines ?",
                "Confirm Archiving ALL selected routines",
                JOptionPane.OK_CANCEL_OPTION
        );

        if (result == JOptionPane.OK_OPTION) {
            for (var routineComponent : routineComponents) {
                var routine = routineComponent.getRoutine();
                archiveRoutine(routineComponent, routine);
            }
            selectedRoutineComponents.clear();
        } else if (result == JOptionPane.CANCEL_OPTION || result == JOptionPane.CLOSED_OPTION) {
            mainframe.appendMessageDEBUG("Cancelled archiving all " + routineComponents.size() + " selected routines");
        }
    }

    public void batchRemoveRoutineComponent(List<RoutineComponent> routineComponents) {

        int result = ScrollableConfirmDialog.show(
                null,
                "Are you sure you want to delete all : " + routineComponents.size() + " selected routines ?",
                "Confirm Deleting ALL selected routines",
                JOptionPane.OK_CANCEL_OPTION
        );

        if (result == JOptionPane.OK_OPTION) {
            for (var routineComponent : routineComponents) {
                var routine = routineComponent.getRoutine();
                deleteRoutine(routineComponent, routine);
            }
            selectedRoutineComponents.clear();
        } else if (result == JOptionPane.CANCEL_OPTION || result == JOptionPane.CLOSED_OPTION) {
            mainframe.appendMessageDEBUG("Cancelled deleting all " + routineComponents.size() + " selected routines");
        }
    }

    /**
     * Will remove the routineComponent from external lists and from the GUI
     */
    private void removeRoutineComponent(RoutineComponent routineComponent) {
        routineComponent.setVisible(false);
        routineBasePanel.remove(routineComponent);
        routineComponents.remove(routineComponent);
        selectedRoutineComponents.remove(routineComponent);
        routines.remove(routineComponent.getRoutine());
    }

    @Override
    public void onSelectRoutine(RoutineComponent routineComponent) {
        if (selectedRoutineComponents.contains(routineComponent)) {
            selectedRoutineComponents.remove(routineComponent);
        } else {
            selectedRoutineComponents.add(routineComponent);
        }
    }

    @Override
    public void onEditRoutine(RoutineComponent routineComponent) {
        var routine = routineComponent.getRoutine();
        Optional<Routine> optionalUpdatedRoutine = openContextAddRoutine(Optional.of(routine));
        if (optionalUpdatedRoutine.isEmpty()) {
            this.mainframe.appendMessageDEBUG("Editing Routine was cancelled");
            return;
        }
        Routine updatedRoutine = optionalUpdatedRoutine.get();

        // Assuming RoutineService has a replaceTask method
        try {
            updatedRoutine = routineService.replaceRoutine(routine, updatedRoutine);
            routineComponent.updateRoutine(updatedRoutine);
        } catch (SQLException e) {
            this.mainframe.appendMessage("Error trying to edit routine " + routine.getName());
            return;
        }

        removeRoutineComponent(routineComponent);
        addRoutineInGUI(updatedRoutine);
        this.mainframe.appendMessage("Updated routine " + routine.getName());
    }

    /**
     * @param routineComponent Component to remove from the UI
     * @param routine          Routine associated to archive from the DB
     */
    private void archiveRoutine(RoutineComponent routineComponent, Routine routine) {
        try {
            routineService.archiveRoutine(routine);
        } catch (SQLException e) {
            mainframe.appendMessage("There was a problem with DB trying to archive routine :" + routine.getName());
            return;
        }

        removeRoutineComponent(routineComponent);
        mainframe.appendMessageDEBUG("Archived routine: " + routine.getName());
    }

    @Override
    public void onArchiveRoutine(RoutineComponent routineComponent) {
        var routine = routineComponent.getRoutine();
        int result = ScrollableConfirmDialog.show(
                null,
                "Are you sure you want to archive this routine : \n" + Helper.prettyPrintUsingGson(routine),
                "Confirm Archiving routine",
                JOptionPane.OK_CANCEL_OPTION
        );

        if (result == JOptionPane.OK_OPTION) {
            try {
                routineService.archiveRoutine(routine);
            } catch (SQLException e) {
                mainframe.appendMessage("There was a problem with DB trying to archive routine :" + routine.getName());
                return;
            }

            removeRoutineComponent(routineComponent);
            mainframe.appendMessageDEBUG("Archived routine: " + routine.getName());
        } else if (result == JOptionPane.CANCEL_OPTION || result == JOptionPane.CLOSED_OPTION) {
            mainframe.appendMessageDEBUG("Cancelled archiving routine: " + routine.getName());
        }
    }

    /**
     * @param routineComponent Component to remove from the UI
     * @param routine          Routine associated to remove from the DB
     */
    private void deleteRoutine(RoutineComponent routineComponent, Routine routine) {
        try {
            routineService.deleteRoutine(routine);
        } catch (SQLException e) {
            mainframe.appendMessage("There was a problem with DB trying to delete routine : " + routine.getName());
            return;
        }

        removeRoutineComponent(routineComponent);
        mainframe.appendMessageDEBUG("Deleted routine: " + routine.getName());
    }


    @Override
    public void onDeleteRoutine(RoutineComponent routineComponent) {
        var routine = routineComponent.getRoutine();
        int result = ScrollableConfirmDialog.show(
                null,
                "Are you sure you want to delete this routine : \n" + Helper.prettyPrintUsingGson(routine),
                "Confirm Deleting routine",
                JOptionPane.OK_CANCEL_OPTION
        );

        if (result == JOptionPane.OK_OPTION) {
            deleteRoutine(routineComponent, routine);
        } else if (result == JOptionPane.CANCEL_OPTION || result == JOptionPane.CLOSED_OPTION) {
            mainframe.appendMessageDEBUG("Cancelled deleting routine: " + routine.getName());
        }
    }


    public void clearRoutinesFromPanel() {
        for (var routineComponent : new ArrayList<>(routineComponents)) {
            removeRoutineComponent(routineComponent);
        }
    }

    public void loadRoutines() {
        routines = new ArrayList<>(routineService.getRoutineSortedByNextDate());
        resetRoutines();
        for (Routine routine : routines) {
            addRoutineInGUI(routine);
        }
    }

    public void resetRoutines() {
        // Assuming RoutineService has a reset method
        routines = routineService.reset(routines);
    }
}