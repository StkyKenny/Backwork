package me.stky.backwork.gui;

import javax.swing.*;
import java.awt.*;

public class TaskPanel extends JPanel {

    public JPanel taskPanel;
    private MainFrame mainframe;

    public TaskPanel(MainFrame mainframe) {
        taskPanel = new JPanel();
        taskPanel.setLayout(new BoxLayout(taskPanel, BoxLayout.Y_AXIS));


        String[] tasks = {
                "something1",
                "something2",
                "something3",
                "something4",
                "something5"
        };

        for (String task : tasks) {
            JCheckBox checkBox = new JCheckBox(task);
            checkBox.setFont(checkBox.getFont().deriveFont(18f));
            checkBox.addActionListener(e -> {
                taskPanel.remove(checkBox);
                mainframe.appendMessage("Completed : " + checkBox.getText());
                taskPanel.revalidate();
                taskPanel.repaint();
            });

            taskPanel.add(checkBox);
        }


        // THIS IS IMPORTANT
        setLayout(new BorderLayout());
        add(taskPanel, BorderLayout.NORTH);
    }

}