package me.stky.backwork.gui;

import me.stky.backwork.model.Task;
import me.stky.backwork.service.TaskService;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class TaskPanel extends JPanel {

    public JPanel taskPanel = new JPanel();
    ;
    private MainFrame mainframe;
    TaskService taskService = TaskService.getInstance();

    List<Task> tasks = new ArrayList<>(taskService.getTasks());

    public TaskPanel(MainFrame mainframe) {
        this.mainframe = mainframe;


        taskPanel.setLayout(new BoxLayout(taskPanel, BoxLayout.Y_AXIS));

        JPanel buttonsPanel = new JPanel();
        var addTaskButton = new JButton("add Task");
        addTaskButton.addActionListener(_ -> this.openContextAddTask());
        buttonsPanel.add(addTaskButton);


        JToggleButton completedToggle = new JToggleButton("Toggle Completed");

        completedToggle.addActionListener(e -> {
            if (completedToggle.isSelected()) {
                // TODO
            } else {
                // TODO
            }
        });


        buttonsPanel.add(completedToggle);

        taskPanel.add(buttonsPanel);

        System.out.println(tasks);
        for (Task task : tasks) {
            addTaskInGUI(task);
        }

        // THIS IS IMPORTANT
        setLayout(new BorderLayout());
        add(buttonsPanel, BorderLayout.NORTH);
        add(taskPanel, BorderLayout.CENTER);
    }

    /**
     * Partially AI-generated to not look up how to dialog and layout in Swing
     */
    public void openContextAddTask() {


        JTextField nameField = new JTextField("Untitled", 15);
        JTextField descriptionField = new JTextField(15);

        JCheckBox javaCheck = new JCheckBox("Java");
        JCheckBox swingCheck = new JCheckBox("Swing");
        JCheckBox sqlCheck = new JCheckBox("SQL");

        JPanel panel2 = new JPanel();
        panel2.setLayout(new BoxLayout(panel2, BoxLayout.Y_AXIS));

        panel2.add(new JLabel("Name:"));
        panel2.add(nameField);

        panel2.add(new JLabel("Description:"));
        panel2.add(descriptionField);

        panel2.add(new JLabel("Skills:"));
        panel2.add(javaCheck);
        panel2.add(swingCheck);
        panel2.add(sqlCheck);

        int result = JOptionPane.showConfirmDialog(
                null,
                panel2,
                "User Information",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (result == JOptionPane.OK_OPTION) {

            String name = nameField.getText();
            String age = descriptionField.getText();

            // Check inputs
            if (name.isBlank()) {
                JOptionPane.showMessageDialog(
                        null,
                        "Can't be untitled"
                );
                return;
            }

            // Checkboxes
            StringBuilder skills = new StringBuilder();

            if (javaCheck.isSelected()) {
                skills.append("Java ");
            }

            if (swingCheck.isSelected()) {
                skills.append("Swing ");
            }

            if (sqlCheck.isSelected()) {
                skills.append("SQL ");
            }
            addTask(new Task(name, ""));
        }
    }

    private void completeTask(Task task, JCheckBox checkBox) {

        taskService.completeTask(task);
        tasks.remove(task);
        taskPanel.remove(checkBox);
    }

    private void addTask(Task task) {
        tasks.add(task); // add in current memory list
        mainframe.appendMessage("---------------------------->");
        mainframe.appendMessage("New task added : " + task.toString());
        mainframe.appendMessageDEBUG("with ID : " + taskService.addTask(task).toString());

        addTaskInGUI(task); // GUI
    }

    private void addTaskInGUI(Task task) {
        JCheckBox checkBox = new JCheckBox(task.getName());
        checkBox.setFont(checkBox.getFont().deriveFont(18f));

        checkBox.addActionListener(e -> {

            String soundName = "src/main/resources/sfx/bloopsfx_cut.wav";

            try {
                AudioInputStream audioInputStream =
                        AudioSystem.getAudioInputStream(new File(soundName));

                Clip clip = AudioSystem.getClip();
                clip.open(audioInputStream);
                clip.start();

            } catch (Exception ex) {
                ex.printStackTrace();
            }

            completeTask(task, checkBox);
            //taskService.removeTask(task);
            tasks.remove(task);
            taskPanel.remove(checkBox);


            mainframe.appendMessage("Completed : " + task);

            taskPanel.revalidate();
            taskPanel.repaint();
        });

        taskPanel.add(checkBox);

        taskPanel.revalidate();
        taskPanel.repaint();
    }

}