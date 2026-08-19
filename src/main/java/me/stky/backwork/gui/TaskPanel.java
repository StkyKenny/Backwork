package me.stky.backwork.gui;

import me.stky.backwork.model.Task;
import me.stky.backwork.model.TaskStatus;
import me.stky.backwork.service.TaskService;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class TaskPanel extends JPanel {

    public JPanel taskPanel = new JPanel();
    private final MainFrame mainframe;
    private final TaskService taskService = TaskService.getInstance();
    private List<Task> tasks = new ArrayList<>(taskService.getTasks());
    private List<TaskComponent> tasksComponents = new ArrayList<>();
    private final JToggleButton completedToggle = new JToggleButton("Toggle Completed ✕");

    public TaskPanel(MainFrame mainframe) {
        this.mainframe = mainframe;
        taskPanel.setLayout(new BoxLayout(taskPanel, BoxLayout.Y_AXIS));

        // ScrollPane
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

        System.out.println(tasks.size());
        for (Task task : tasks) {
            addTaskInGUI(task);
        }

        // THIS IS IMPORTANT
        setLayout(new BorderLayout());
        add(buttonsPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
    }

    /**
     * Partially AI-generated to not look up how to dialog and layout in Swing
     */
    public void openContextAddTask() {
        JTextField nameField = new JTextField("Untitled", 15);
        JTextField descriptionField = new JTextField(15);

        JCheckBox javaCheck = new JCheckBox("Java");

        JPanel panel2 = new JPanel();
        panel2.setLayout(new BoxLayout(panel2, BoxLayout.Y_AXIS));

        panel2.add(new JLabel("Name:"));
        panel2.add(nameField);

        panel2.add(new JLabel("Description:"));
        panel2.add(descriptionField);

        panel2.add(new JLabel("Skills:"));
        panel2.add(javaCheck);

        int result = JOptionPane.showConfirmDialog(
                null,
                panel2,
                "User Information",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (result == JOptionPane.OK_OPTION) {
            String name = nameField.getText();
            // Check inputs
            if (name.isBlank()) {
                JOptionPane.showMessageDialog(
                        null,
                        "Can't be untitled"
                );
                return;
            }
            addTask(new Task(name, ""));
        }
    }

    /**
     * Complete, not remove
     *
     * @param task
     * @param taskComponent
     */
    private void completeTask(Task task, TaskComponent taskComponent) {
        taskService.completeTask(task);
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
        taskPanel.add(taskComponent);
        taskPanel.revalidate();
        taskPanel.repaint();
    }

}