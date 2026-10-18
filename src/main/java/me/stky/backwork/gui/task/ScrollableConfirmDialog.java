package me.stky.backwork.gui.task;

import javax.swing.*;
import java.awt.*;

/*
    AI-generated, just for confirm dialog that have long text
 */
public class ScrollableConfirmDialog extends JOptionPane {

    private static final int MAX_WIDTH = 500;
    private static final int MAX_HEIGHT = 700;

    private ScrollableConfirmDialog() {
        // Utility class
    }

    public static int show(
            Component parent,
            String message,
            String title,
            int optionType) {


        JTextArea textArea = new JTextArea(message);
        textArea.setEditable(false);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setCaretPosition(0);

        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(MAX_WIDTH, Short.MAX_VALUE));
        Dimension maximumSize = new Dimension(MAX_WIDTH, MAX_HEIGHT);


        JOptionPane optionPane = new JOptionPane(
                scrollPane,
                JOptionPane.QUESTION_MESSAGE,
                optionType
        );

        JDialog dialog = optionPane.createDialog(parent, title);
        dialog.setResizable(true);
        dialog.pack();

        // Enforce the maximum window dimensions
        dialog.setMaximumSize(maximumSize);

        Dimension packedSize = dialog.getSize();
        int width = Math.min(packedSize.width, maximumSize.width);
        int height = Math.min(packedSize.height, maximumSize.height);

        dialog.setSize(width, height);
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);
        dialog.dispose();

        Object value = optionPane.getValue();

        return value instanceof Integer
                ? (Integer) value
                : JOptionPane.CLOSED_OPTION;
    }

}
