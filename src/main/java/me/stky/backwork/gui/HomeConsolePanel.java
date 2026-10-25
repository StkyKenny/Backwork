package me.stky.backwork.gui;

import javax.swing.*;
import javax.swing.text.StyledDocument;
import java.awt.*;

public class HomeConsolePanel extends JPanel {

    private final JTextPane output = new JTextPane();
    public StyledDocument outputDocument = output.getStyledDocument();

    public HomeConsolePanel() {
        setLayout(new BorderLayout());
        add(new JLabel("Home tab"), BorderLayout.CENTER);
        add(new JScrollPane(output), BorderLayout.CENTER);
        output.setEditable(false);

    }
}
