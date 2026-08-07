package me.stky.backwork.gui;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainFrame extends JFrame {


    private JTextPane output = new JTextPane();
    private JTextPane outputDebug = new JTextPane();
    private JTabbedPane tabbedPane = new JTabbedPane();
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    int ScreenWidth = screenSize.width;
    int ScreenHeight = screenSize.height;
    StyledDocument outputDocument = output.getStyledDocument();
    StyledDocument outputDebugDocument = outputDebug.getStyledDocument();

    SimpleAttributeSet titleStyle = new SimpleAttributeSet();
    SimpleAttributeSet logStyle = new SimpleAttributeSet();


    private SimpleAttributeSet createTitleStyle() {

        SimpleAttributeSet title = new SimpleAttributeSet();

        StyleConstants.setAlignment(
                title,
                StyleConstants.ALIGN_CENTER
        );

        StyleConstants.setBold(
                title,
                true
        );

        return title;
    }

    private static final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");


    public MainFrame() {

        StyleConstants.setAlignment(titleStyle, StyleConstants.ALIGN_CENTER);
        // Title style
        StyleConstants.setAlignment(titleStyle, StyleConstants.ALIGN_CENTER);
        StyleConstants.setBold(titleStyle, true);
        StyleConstants.setFontSize(titleStyle, 16);

        setTitle("Spring + Swing Test");
        setSize(500, (int) (ScreenHeight * 0.8));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JButton button = new JButton("Test Spring");

        output.setEditable(false);
        output.setText("");

        button.addActionListener(e -> {
            String result = "test";
            this.appendTitle(result);
        });
        add(button, BorderLayout.NORTH);

        // Home Panel
        JPanel homePanel = new JPanel(new BorderLayout());
        homePanel.add(new JLabel("Home tab"), BorderLayout.CENTER);
        homePanel.add(new JScrollPane(output), BorderLayout.CENTER);

        // Debug Panel
        JPanel logsPanel = new JPanel(new BorderLayout());
        logsPanel.add(new JLabel("Logs tab"), BorderLayout.CENTER);
        logsPanel.add(new JScrollPane(outputDebug), BorderLayout.CENTER);

        // Settings / Action Tab
        JPanel settingsPanel = new JPanel(new BorderLayout());
        settingsPanel.add(new JLabel("Settings tab"), BorderLayout.CENTER);

        tabbedPane.addTab("Home", homePanel);
        tabbedPane.addTab("Logs", logsPanel);
        tabbedPane.addTab("Settings", settingsPanel);

        add(tabbedPane, BorderLayout.CENTER);
    }

    /**
     * Output message to home tab and Debug Tab
     * Also has timestamp
     *
     * @param message
     */
    public void appendMessage(String message) {
        message = "[" + timeFormat.format(new Date()) + "] " + message;
        appendStyledmessage(message, outputDocument, logStyle);
        appendMessageDEBUG(message);
    }

    public void appendStyledmessage(String message, StyledDocument document, SimpleAttributeSet style) {
        SwingUtilities.invokeLater(() -> {
            try {
                int start = document.getLength();
                document.insertString(
                        start,
                        message + "\n",
                        style
                );
                document.setParagraphAttributes(
                        start,
                        message.length(),
                        style,
                        false
                );

                // auto scroll
                //output.setCaretPosition(document.getLength());
            } catch (BadLocationException e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * Output message to DEBUG Panel
     *
     * @param message
     */
    public void appendMessageDEBUG(String message) {
        appendStyledmessage(message, outputDebugDocument, logStyle);
    }

    public void appendTitle(String title) {
        appendStyledmessage(title, outputDocument, titleStyle);
        appendDebugTitle(title);
    }

    public void appendDebugTitle(String title) {
        appendStyledmessage(title, outputDebugDocument, titleStyle);
    }

}