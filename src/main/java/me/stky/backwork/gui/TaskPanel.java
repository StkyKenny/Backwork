package me.stky.backwork.gui;

import com.toedter.calendar.JDateChooser;
import me.stky.backwork.model.IntervalUnit;
import me.stky.backwork.model.RecurringTaskInfo;
import me.stky.backwork.model.Task;
import me.stky.backwork.model.TaskStatus;
import me.stky.backwork.service.Helper;
import me.stky.backwork.service.TaskService;
import org.bson.types.ObjectId;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class TaskPanel extends JPanel implements TaskListener {

    public JPanel taskPanel = new JPanel();
    public JPanel taskBasePanel = new JPanel();
    public JPanel taskRecurrentPanel = new JPanel();
    private final MainFrame mainframe;
    private final TaskService taskService = TaskService.getInstance();
    private List<Task> tasks = new ArrayList<>(taskService.getTasks());

    private final List<TaskComponent> tasksComponents = new ArrayList<>();
    private final List<TaskComponent> selectedTasksComponents = new ArrayList<>();
    private final JToggleButton completedToggle = new JToggleButton("Toggle Completed ✕");

    public TaskPanel(MainFrame mainframe) {
        this.mainframe = mainframe;
        taskPanel.setLayout(new BoxLayout(taskPanel, BoxLayout.Y_AXIS));
        taskRecurrentPanel.setLayout(new BoxLayout(taskRecurrentPanel, BoxLayout.Y_AXIS));
        taskBasePanel.setLayout(new BoxLayout(taskBasePanel, BoxLayout.Y_AXIS));

        // ScrollPane
        taskPanel.add(taskBasePanel);
        taskPanel.add(new JSeparator());
        taskPanel.add(taskRecurrentPanel);

        JScrollPane scrollPane = new JScrollPane(taskPanel);
        scrollPane.setVerticalScrollBarPolicy(
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS
        );
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        JPanel buttonsPanel = new JPanel();
        var addTaskButton = new JButton("add Task");
        addTaskButton.addActionListener(_ -> addTask(this.openContextAddTask()));
        buttonsPanel.add(addTaskButton);


        completedToggle.addActionListener(_ -> {
            if (completedToggle.isSelected()) {
                for (var taskComponent : tasksComponents) {
                    taskComponent.setVisible(true);
                }
                completedToggle.setText("Toggle Completed ✓");
            } else {
                for (var taskComponent : tasksComponents) {
                    if (taskComponent.getTask().getStatus() == TaskStatus.COMPLETED) {
                        taskComponent.setVisible(false);
                    }
                }
                completedToggle.setText("Toggle Completed ✕");
            }

            taskPanel.revalidate();
            taskPanel.repaint();
        });


        buttonsPanel.add(completedToggle);
        taskPanel.add(buttonsPanel);

        var refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(_ -> refreshTasks());
        buttonsPanel.add(refreshButton);

        var batchOpsButton = new JButton("Batch ops");
        batchOpsButton.addActionListener(_ -> showBatchOperationsMenu(batchOpsButton));
        buttonsPanel.add(batchOpsButton);

        for (Task task : tasks.reversed()) {
            addTaskInGUI(task);
        }

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
     * Wrapper of openContextAddTask(Optional<Task> presetTask)  for setting no preset values
     *
     * @return Optional Task created
     */
    public Optional<Task> openContextAddTask() {
        return openContextAddTask(Optional.empty());
    }

    /**
     * @param presetTask An Optional Task used to set default values
     * @return Optional Task created
     */
    public Optional<Task> openContextAddTask(Optional<Task> presetTask) {
        Task defaultValuesTask = new Task(Helper.rdnName(), "", false);
        if (presetTask.isPresent()) {
            defaultValuesTask = presetTask.get();
        }

        JPanel panel2 = new JPanel(new GridBagLayout());
        panel2.setMinimumSize(new Dimension(400, 50));
        GridBagConstraints gbc = createGridConstraints();
        int rowIndex = 0;

        panel2.add(new JLabel("Name :"), gbc);
        JTextField nameField = new JTextField(defaultValuesTask.getName());
        panel2.add(nameField, setGbc(gbc, 1, rowIndex, 1));


        rowIndex += 1;
        panel2.add(new JLabel("Description :"), setGbc(gbc, 0, rowIndex, 0));
        JTextField descriptionField = new JTextField(defaultValuesTask.getDescription(), 20);
        panel2.add(descriptionField, setGbc(gbc, 1, rowIndex, 1));


        rowIndex += 1;
        panel2.add(new JLabel("Tags (comma-separated):"), setGbc(gbc, 0, rowIndex, 0));
        JTextField tagsField = new JTextField();//String.join(",", defaultValuesTask.getTags()));
        panel2.add(tagsField, setGbc(gbc, 1, rowIndex, 1));

        rowIndex += 1;
        panel2.add(new JLabel("Deadline"), setGbc(gbc, 0, rowIndex, 0));
        JDateChooser deadlineCalendar = new JDateChooser();
        deadlineCalendar.setDateFormatString("yyyy-MM-dd");
        panel2.add(deadlineCalendar, setGbc(gbc, 1, rowIndex, 1));

        rowIndex += 1;
        panel2.add(new JLabel("Time constrained ?"), setGbc(gbc, 0, rowIndex, 0));
        JCheckBox isTimeConstrainedField = new JCheckBox("");
        isTimeConstrainedField.setEnabled(defaultValuesTask.getIsTimeConstrained());
        panel2.add(isTimeConstrainedField, setGbc(gbc, 1, rowIndex, 1));

        rowIndex += 1;
        panel2.add(new JLabel("Is Recurrent ?"), setGbc(gbc, 0, rowIndex, 0));
        JCheckBox recurrentToggle = new JCheckBox("");
        panel2.add(recurrentToggle, setGbc(gbc, 1, rowIndex, 1));

        List<JComponent> recurrentFieldsComponents = new ArrayList<>();
        rowIndex += 1;
        JPanel recurrentPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        IntervalUnit[] choices = IntervalUnit.values();

        JSpinner recurrentAmount =
                new JSpinner(new SpinnerNumberModel(1, 1, 9999, 1));

        JLabel recurrentLabel = new JLabel("Every:");
        panel2.add(recurrentLabel, setGbc(gbc, 0, rowIndex, 0));
        recurrentFieldsComponents.add(recurrentLabel);

        final JComboBox<IntervalUnit> cb = new JComboBox<>(choices);
        recurrentPanel.add(recurrentAmount);
        recurrentPanel.add(cb);
        recurrentFieldsComponents.add(recurrentPanel);
        panel2.add(recurrentPanel, setGbc(gbc, 1, rowIndex, 1, 3));

        rowIndex += 1;
        var startingDateLabel = new JLabel("Starting Date:");
        recurrentFieldsComponents.add(startingDateLabel);
        panel2.add(startingDateLabel, setGbc(gbc, 0, rowIndex, 0));
        JDateChooser calendar = new JDateChooser(Date.from(Instant.now()));
        calendar.setDateFormatString("yyyy-MM-dd");
        recurrentFieldsComponents.add(calendar);
        panel2.add(calendar, setGbc(gbc, 1, rowIndex, 1, 5));


        for (JComponent component : recurrentFieldsComponents) {
            component.setVisible(recurrentToggle.isSelected());
        }

        Runnable showRecurrentMenu = () -> {
            for (JComponent component : recurrentFieldsComponents) {
                component.setVisible(recurrentToggle.isSelected());
            }
            isTimeConstrainedField.setEnabled(!recurrentToggle.isSelected());
            // Recalculate the dialog size
            Window window = SwingUtilities.getWindowAncestor(recurrentToggle);
            if (window != null) {
                window.pack();
            }
        };

        recurrentToggle.addActionListener(_ -> {
            showRecurrentMenu.run();
        });

        if (defaultValuesTask.getRecurringTaskInfo() != null) {
            recurrentToggle.setSelected(true);
            showRecurrentMenu.run();
            recurrentAmount.setValue(defaultValuesTask.getRecurringTaskInfo().getInterval());
            cb.setSelectedItem(IntervalUnit.valueOf(defaultValuesTask.getRecurringTaskInfo().getIntervalUnit().name()));
            calendar.setDate(Date.from(defaultValuesTask.getRecurringTaskInfo().getFirstStartedDate()));
        }

        int result = JOptionPane.showConfirmDialog(
                null,
                panel2,
                "Add new Task",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (result == JOptionPane.OK_OPTION) {
            String name = nameField.getText();
            // Check inputs
            if (name != null && name.isBlank()) {
                JOptionPane.showMessageDialog(
                        null,
                        "Can't be untitled"
                );
                return Optional.empty();
            }

            Instant deadlineDate = null;
            if (deadlineCalendar.getDate() != null) {
                deadlineDate = LocalDate.ofInstant(deadlineCalendar.getDate().toInstant(), ZoneId.systemDefault()).atStartOfDay(ZoneId.systemDefault()).toInstant();

            }
            if (!recurrentToggle.isSelected()) {

                return Optional.of(new Task(name, descriptionField.getText(), deadlineDate, isTimeConstrainedField.isSelected()));
            } else {
                if (calendar.getDate() == null) {
                    this.mainframe.appendMessage("Please enter or select a valid date");
                    return Optional.empty();
                }
                int amount = (Integer) recurrentAmount.getValue();
                if (amount <= 0) {
                    this.mainframe.appendMessage("Please enter a positive number for the recurrence amount.");
                    return Optional.empty();
                }
                RecurringTaskInfo recurringTaskInfo = new RecurringTaskInfo(amount, (IntervalUnit) Objects.requireNonNull(cb.getSelectedItem()), Instant.now());
                return Optional.of(new Task(name, descriptionField.getText(), deadlineDate, recurringTaskInfo));
            }
        }
        return Optional.empty();
    }

    /**
     * Complete, not remove
     *
     * @param task
     * @param taskComponent
     */
    private void completeTask(Task task, TaskComponent taskComponent) {
        try {
            Task updatedTask = taskService.completeTask(task);
            taskComponent.updateTask(updatedTask);
        } catch (SQLException e) {
            mainframe.appendMessage("There was a problem with DB trying to complete the task :" + task.getID());
            return;
        }
        if (!completedToggle.isSelected()) {
            taskComponent.setVisible(false);
        }
        taskPanel.revalidate();
        taskPanel.repaint();
    }

    private void addTask(Optional<Task> taskOpt) {
        if (taskOpt.isEmpty()) {
            return;
        }
        Task task = taskOpt.get();
        String upsertedID = null;
        try {
            upsertedID = (taskService.addTask(taskOpt.get())).toString();
            tasks.add(task); // add in current memory list
        } catch (SQLException e) {
            mainframe.appendMessage("---------------------------->");
            mainframe.appendMessage("Error trying to add this new Task : " + task);
        }
        mainframe.appendMessageDEBUG("---------------------------->");
        mainframe.appendMessageDEBUG("New task added : " + task);
        mainframe.appendMessageDEBUG("with ID : " + upsertedID);
        addTaskInGUI(task); // GUI

    }

    private void addTaskInGUI(Task task) {
        TaskComponent taskComponent = new TaskComponent(this.mainframe, this, task,
                component -> {
                    //completeTask(task, component);
                    mainframe.appendMessage("Completed : " + task);
                });

        if (!completedToggle.isSelected() && task.getStatus() == TaskStatus.COMPLETED) {
            taskComponent.setVisible(false);
            taskPanel.revalidate();
            taskPanel.repaint();
        }
        tasksComponents.add(taskComponent);
        if (task.getRecurringTaskInfo() != null) {
            taskBasePanel.add(taskComponent, 0);
        } else {
            taskRecurrentPanel.add(taskComponent, 0);
        }
        taskPanel.revalidate();
        taskPanel.repaint();
    }

    /**
     * It will check and update recurring tasks clearing them
     */
    public void refreshTasks() {
        var updatedTasks = taskService.refresh(tasks);
        Map<String, Task> tasksById = updatedTasks.stream()
                .filter(task -> updatedTasks.stream().map(Task::getID).collect(Collectors.toSet()).contains(task.getID()))
                .collect(Collectors.toMap(
                        Task::getID,
                        task -> task
                ));

        tasksComponents.stream()
                .filter(component -> tasksById.containsKey(component.getTask().getID()))
                .forEach(component ->
                        {
                            component.updateTask(tasksById.get(component.getTask().getID()));
                            component.setVisible(true);
                        }
                );
    }

    private void showBatchOperationsMenu(JButton button) {
        JPopupMenu menu = new JPopupMenu();


        JMenuItem choice2 = new JMenuItem("Delete Task");
        JMenuItem choicetest = new JMenuItem("Test");

        choice2.addActionListener(_ -> batchRemoveTaskComponent(selectedTasksComponents));
        choicetest.addActionListener(_ -> mainframe.appendMessage("Selected : " + selectedTasksComponents.size() + " tasks."));

        menu.add(choice2);
        menu.add(choicetest);

        menu.show(button, 0, button.getHeight());
    }

    public void batchRemoveTaskComponent(List<TaskComponent> taskComponents) {

        int result = JOptionPane.showConfirmDialog(
                null,
                "Are you sure you want to delete all : " + taskComponents.size() + " selected tasks ?",
                "Confirm Deleting ALL selected tasks",
                JOptionPane.OK_CANCEL_OPTION
        );


        if (result == JOptionPane.OK_OPTION) {

            for (var taskComponent : taskComponents) {
                var task = taskComponent.getTask();
                deleteTask(taskComponent, task);
            }
            selectedTasksComponents.clear();

        } else if (result == JOptionPane.CANCEL_OPTION || result == JOptionPane.CLOSED_OPTION) {
            mainframe.appendMessageDEBUG("Cancelled deleting all " + taskComponents.size() + " selected tasks");
        }
    }

    /**
     * Will remove the taskComponent from external lists and from the GUI
     *
     * @param taskComponent to be removed
     */
    private void removeTaskComponent(TaskComponent taskComponent) {
        taskComponent.setVisible(false);
        if (taskComponent.getTask().getRecurringTaskInfo() == null) {
            taskRecurrentPanel.remove(taskComponent);
        } else {
            taskBasePanel.remove(taskComponent);
        }
        tasksComponents.remove(taskComponent);
        tasks.remove(taskComponent.getTask());
    }

    @Override
    public void onSelectTask(TaskComponent taskComponent) {
        if (selectedTasksComponents.contains(taskComponent)) {
            selectedTasksComponents.remove(taskComponent);
        } else {
            selectedTasksComponents.add(taskComponent);
        }
        //taskComponent.toggleSelected();
    }

    @Override
    public void onEditTask(TaskComponent taskComponent) {

        var task = taskComponent.getTask();
        Optional<Task> optionalUpdatedTask = openContextAddTask(Optional.of(task));
        if (optionalUpdatedTask.isEmpty()) {
            this.mainframe.appendMessageDEBUG("Editing Task was cancelled");
            return;
        }
        Task updatedTask = optionalUpdatedTask.get();

        ObjectId updatedID;
        try {
            updatedID = taskService.replaceTask(task, updatedTask);
        } catch (SQLException e) {
            this.mainframe.appendMessage("Error trying to edit task " + task.getID());
            return;
        }

        removeTaskComponent(taskComponent);

        addTaskInGUI(updatedTask);
        this.mainframe.appendMessage("Updated task " + task.getID());
        this.mainframe.appendMessageDEBUG("\\_ ObjectID : " + updatedID);

        taskComponent.revalidate();
        taskComponent.repaint();
        revalidate();
        repaint();
    }

    @Override
    public void onArchiveTask(TaskComponent taskComponent) {
        var task = taskComponent.getTask();
        int result = JOptionPane.showConfirmDialog(
                null,
                "Are you sure you want to archive this task : \n" + Helper.prettyPrintUsingGson(task),
                "Confirm Archiving task",
                JOptionPane.OK_CANCEL_OPTION
        );

        if (result == JOptionPane.OK_OPTION) {
            try {
                taskService.archiveTask(task);
            } catch (SQLException e) {
                mainframe.appendMessage("There was a problem with DB trying to archive task :" + task.getID());
                return;
            }

            removeTaskComponent(taskComponent);
            mainframe.appendMessageDEBUG("Archived  task: " + task.getName());
        } else if (result == JOptionPane.CANCEL_OPTION || result == JOptionPane.CLOSED_OPTION) {
            mainframe.appendMessageDEBUG("Cancelled archiving task: " + task.getName());
        }
    }

    /**
     * @param taskComponent Component to remove from the UI
     * @param task          Task associated to remove from the DB
     */
    private void deleteTask(TaskComponent taskComponent, Task task) {
        try {
            taskService.deleteTask(task);
        } catch (SQLException e) {
            mainframe.appendMessage("There was a problem with DB trying to delete task : " + task.getID());
            return;
        }

        removeTaskComponent(taskComponent);
        mainframe.appendMessageDEBUG("Deleted task: " + task.getName());
    }

    @Override
    public void onDelete(TaskComponent taskComponent) {
        var task = taskComponent.getTask();
        int result = JOptionPane.showConfirmDialog(
                null,
                "Are you sure you want to delete this task : \n" + Helper.prettyPrintUsingGson(task),
                "Confirm Deleting task",
                JOptionPane.OK_CANCEL_OPTION
        );

        if (result == JOptionPane.OK_OPTION) {
            deleteTask(taskComponent, task);
        } else if (result == JOptionPane.CANCEL_OPTION || result == JOptionPane.CLOSED_OPTION) {
            mainframe.appendMessageDEBUG("Cancelled deleting task: " + task.getName());
        }
    }

}