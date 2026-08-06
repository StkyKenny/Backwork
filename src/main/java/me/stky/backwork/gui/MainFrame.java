package me.stky.backwork.gui;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {


    JTextArea output = new JTextArea();

    public MainFrame() {

        setTitle("Spring + Swing Test");
        setSize(500, 300);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JButton button = new JButton("Test Spring");

        output.setEditable(false);

        button.addActionListener(e -> {
            String result = "test";
            output.append(result + "\n");
        });


        add(button, BorderLayout.NORTH);
        add(new JScrollPane(output), BorderLayout.CENTER);
    }

    public void appendMessage(String message) {
        SwingUtilities.invokeLater(() ->
                output.append(message + "\n")
        );
    }
}