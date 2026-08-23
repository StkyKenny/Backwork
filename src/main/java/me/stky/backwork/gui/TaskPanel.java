package me.stky.backwork.gui;

import com.toedter.calendar.JDateChooser;
import me.stky.backwork.model.IntervalUnit;
import me.stky.backwork.model.RecurringTaskInfo;
import me.stky.backwork.model.Task;
import me.stky.backwork.model.TaskStatus;
import me.stky.backwork.service.TaskService;

import javax.swing.*;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.*;
import java.util.List;

public class TaskPanel extends JPanel {

    public JPanel taskPanel = new JPanel();
    private final MainFrame mainframe;
    private final TaskService taskService = TaskService.getInstance();
    private List<Task> tasks = new ArrayList<>(taskService.getTasks());

    private List<TaskComponent> tasksComponents = new ArrayList<>();
    private final JToggleButton completedToggle = new JToggleButton("Toggle Completed ✕");
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

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
        JTextField nameField = new JTextField(rdnName());
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

        rowIndex += 1;
        JPanel recurrentPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        IntervalUnit[] choices = IntervalUnit.values();

        JSpinner recurrentAmount =
                new JSpinner(new SpinnerNumberModel(1, 1, 9999, 1));

        JLabel recurrentLabel = new JLabel("Every:");
        final JComboBox<IntervalUnit> cb = new JComboBox<IntervalUnit>(choices);
        recurrentPanel.add(recurrentLabel);
        recurrentPanel.add(recurrentAmount);
        recurrentPanel.add(cb);
        panel2.add(recurrentPanel, setGbc(gbc, 0, rowIndex, 1, 3));

        recurrentPanel.setVisible(recurrentToggle.isSelected());


        rowIndex += 1;
        panel2.add(new JLabel("Starting Date:"), setGbc(gbc, 0, rowIndex, 0));
        JTextField startingDateField = new JTextField(dateFormat.format(Date.from(Instant.now())));
        JDateChooser calendar = new JDateChooser();
        calendar.setDateFormatString("yyyy-MM--dd");
        panel2.add(calendar, setGbc(gbc, 1, rowIndex, 1, 5));

        recurrentToggle.addActionListener(e -> {
            recurrentPanel.setVisible(recurrentToggle.isSelected());
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
            if (recurrentToggle.isSelected()) {
                int amount = (Integer) recurrentAmount.getValue();
                if (amount <= 0) {
                    this.mainframe.appendMessage("Please enter a positive number for the recurrence amount.");
                    return;
                }
                RecurringTaskInfo recurringTaskInfo = new RecurringTaskInfo(amount, (IntervalUnit) Objects.requireNonNull(cb.getSelectedItem()), Instant.now());
                addTask(new Task(name, descriptionField.getText(), recurringTaskInfo));
            } else {
                addTask(new Task(name, descriptionField.getText(), isTimeConstrainedField.isSelected()));
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
        taskPanel.add(taskComponent, 0);
        taskPanel.revalidate();
        taskPanel.repaint();
    }

    private String rdnName() {
        String alphabet = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
        StringBuilder rdnString = new StringBuilder();
        Random random = new Random();

        for (int i = 0; i < 6 + random.nextInt(10); i++) {
            int index = random.nextInt(alphabet.length());
            rdnString.append(alphabet.charAt(index));
        }
        return rdnString.toString();
    }
}