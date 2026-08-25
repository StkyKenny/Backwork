package me.stky.backwork.gui;

import com.toedter.calendar.JDateChooser;
import me.stky.backwork.model.IntervalUnit;
import me.stky.backwork.model.RecurringTaskInfo;
import me.stky.backwork.model.Task;
import me.stky.backwork.model.TaskStatus;
import me.stky.backwork.service.Helper;
import me.stky.backwork.service.TaskService;

import javax.swing.*;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class TaskPanel extends JPanel {

    public JPanel taskPanel = new JPanel();
    public JPanel taskBasePanel = new JPanel();
    public JPanel taskRecurrentPanel = new JPanel();
    private final MainFrame mainframe;
    private final TaskService taskService = TaskService.getInstance();
    private List<Task> tasks = new ArrayList<>(taskService.getTasks());

    private final List<TaskComponent> tasksComponents = new ArrayList<>();
    private final JToggleButton completedToggle = new JToggleButton("Toggle Completed ✕");
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

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
        addTaskButton.addActionListener(_ -> this.openContextAddTask());
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
        refreshButton.addActionListener(_ -> {
            refreshTasks();
        });
        buttonsPanel.add(refreshButton);

        System.out.println(tasks.size());
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

    public void openContextAddTask() {


        JPanel panel2 = new JPanel(new GridBagLayout());
        panel2.setMinimumSize(new Dimension(400, 50));
        GridBagConstraints gbc = createGridConstraints();
        int rowIndex = 0;

        panel2.add(new JLabel("Name :"), gbc);
        JTextField nameField = new JTextField(Helper.rdnName());
        panel2.add(nameField, setGbc(gbc, 1, rowIndex, 1));


        rowIndex += 1;
        panel2.add(new JLabel("Description :"), setGbc(gbc, 0, rowIndex, 0));
        JTextField descriptionField = new JTextField(20);
        panel2.add(descriptionField, setGbc(gbc, 1, rowIndex, 1));


        rowIndex += 1;
        panel2.add(new JLabel("Tags (comma-separated):"), setGbc(gbc, 0, rowIndex, 0));
        JTextField tagsField = new JTextField();
        panel2.add(tagsField, setGbc(gbc, 1, rowIndex, 1));

        rowIndex += 1;
        panel2.add(new JLabel("Time constrained ?"), setGbc(gbc, 0, rowIndex, 0));
        JCheckBox isTimeConstrainedField = new JCheckBox("");
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

        final JComboBox<IntervalUnit> cb = new JComboBox<IntervalUnit>(choices);
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

        recurrentToggle.addActionListener(e -> {
            for (JComponent component : recurrentFieldsComponents) {
                component.setVisible(recurrentToggle.isSelected());
            }
            isTimeConstrainedField.setEnabled(!recurrentToggle.isSelected());
            // Recalculate the dialog size
            Window window = SwingUtilities.getWindowAncestor(recurrentToggle);
            if (window != null) {
                window.pack();
            }
        });

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
                return;
            }
            if (!recurrentToggle.isSelected()) {
                addTask(new Task(name, descriptionField.getText(), isTimeConstrainedField.isSelected()));
            } else {
                if (calendar.getDate() == null) {
                    this.mainframe.appendMessage("Please enter or select a valid date");
                    return;
                }
                int amount = (Integer) recurrentAmount.getValue();
                if (amount <= 0) {
                    this.mainframe.appendMessage("Please enter a positive number for the recurrence amount.");
                    return;
                }
                RecurringTaskInfo recurringTaskInfo = new RecurringTaskInfo(amount, (IntervalUnit) Objects.requireNonNull(cb.getSelectedItem()), Instant.now());
                addTask(new Task(name, descriptionField.getText(), recurringTaskInfo));
            }
        }
    }

    /**
     * Complete, not remove
     *
     * @param task
     * @param taskComponent
     */
    private void completeTask(Task task, TaskComponent taskComponent) {
        var updatedTask = taskService.completeTask(task);
        taskComponent.updateTask(updatedTask);
        if (!completedToggle.isSelected()) {
            taskComponent.setVisible(false);
        }
        taskPanel.revalidate();
        taskPanel.repaint();
    }

    private void addTask(Task task) {
        tasks.add(task); // add in current memory list
        mainframe.appendMessage("---------------------------->");
        mainframe.appendMessage("New task added : " + task.toString());
        mainframe.appendMessageDEBUG("with ID : " + taskService.addTask(task).toString());

        addTaskInGUI(task); // GUI
    }

    private void addTaskInGUI(Task task) {
        TaskComponent taskComponent = new TaskComponent(this.mainframe, task,
                component -> {
                    completeTask(task, component);
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
    private void refreshTasks() {
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
}