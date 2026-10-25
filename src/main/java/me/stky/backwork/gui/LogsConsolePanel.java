package me.stky.backwork.gui;

import javax.swing.*;
import javax.swing.text.StyledDocument;
import java.awt.*;

public class LogsConsolePanel extends JPanel {

    private final JTextPane outputDebug = new JTextPane();
    public StyledDocument outputDebugDocument = outputDebug.getStyledDocument();

    public LogsConsolePanel() {
        setLayout(new BorderLayout());
        add(new JLabel("Logs tab"), BorderLayout.CENTER);
        add(new JScrollPane(outputDebug), BorderLayout.CENTER);
        outputDebug.setEditable(false);
    }
}
