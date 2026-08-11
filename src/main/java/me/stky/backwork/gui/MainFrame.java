package me.stky.backwork.gui;

import me.stky.backwork.BackworkApplication;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Date;

import static me.stky.backwork.gui.GuiConfig.*;

public class MainFrame extends JFrame {


    private final int serverPort = 8187;
    private ConfigurableApplicationContext context;

    private final JTextPane output = new JTextPane();
    private final JTextPane outputDebug = new JTextPane();
    private final JTabbedPane tabbedPane = new JTabbedPane();
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
        setSize(GUI_WIDTH, (int) (ScreenHeight * 0.8));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JButton buttonTest = new JButton("Test Spring");

        output.setEditable(false);
        output.setText("");

        buttonTest.addActionListener(_ -> {
            String result = "test";
            this.appendTitle(result);
        });
        add(buttonTest, BorderLayout.NORTH);

        // Home Panel
        JPanel homePanel = new JPanel(new BorderLayout());
        homePanel.add(new JLabel("Home tab"), BorderLayout.CENTER);
        homePanel.add(new JScrollPane(output), BorderLayout.CENTER);

        // Logs Panel
        JPanel logsPanel = new JPanel(new BorderLayout());
        logsPanel.add(new JLabel("Logs tab"), BorderLayout.CENTER);
        logsPanel.add(new JScrollPane(outputDebug), BorderLayout.CENTER);

        // Settings / Action Tab
        JPanel settingsPanel = new JPanel(new GridLayout(2, 3, 10, 10));
        settingsPanel.add(new JLabel("Settings tab"), BorderLayout.CENTER);

        // DEBUG / Action Tab
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));

        // First grid
        ButtonsSetPanel springDebug = new ButtonsSetPanel(0, GUI_WIDTH / (BUTTON_WIDTH + H_GAP), H_GAP, V_GAP);
        springDebug.addButton("Start Spring", _ -> {
            if (context != null && context.isRunning()) {
                appendMessage("Spring is already running");
                return;
            }
            appendMessage("Spring starting ...");
            Thread.startVirtualThread(() -> {
                context = SpringApplication.run(BackworkApplication.class);
                /*GUIService guiService = context.getBean(GUIService.class);
                guiService.setMainFrame(this);*/
                context.publishEvent(new GuiReadyEvent());
                SwingUtilities.invokeLater(() ->
                        appendMessage("Spring has started")
                );
            });
        });
        springDebug.addButton("Stop Spring", _ -> {
            appendMessage("Spring closing ...");
            context.close();
            appendMessage("Spring closed");
        });

        springDebug.addButton("Swagger", _ -> {
            var link = "http://localhost:" + serverPort + "/swagger-ui/index.html";
            try {
                Desktop.getDesktop().browse(new URI(link));
            } catch (Exception ex) {
                appendMessage("Failed to open " + link);
            }
        });

        springDebug.addButton("Actuator", _ -> {
            var link = "http://localhost:" + serverPort + "/actuator";
            try {
                Desktop.getDesktop().browse(new URI(link));
            } catch (Exception ex) {
                appendMessage("Failed to open " + link);
            }
        });

        springDebug.addButton("Health", _ -> {
            var link = "http://localhost:" + serverPort + "/actuator/health";
            try {
                Desktop.getDesktop().browse(new URI(link));
            } catch (Exception ex) {
                appendMessage("Failed to open " + link);
            }
        });
        springDebug.addButton("Message", _ -> appendMessage("Test message"));
        springDebug.addButton("Debug message", _ -> appendMessageDEBUG("Test message (DEBUG only)"));

        ButtonsSetPanel otherApps = new ButtonsSetPanel(0, GUI_WIDTH / (BUTTON_WIDTH + H_GAP), H_GAP, V_GAP);
        otherApps.addButton("Relaytd", _ -> {
            appendMessage("Relaytd starting ...");
            Thread.startVirtualThread(() -> {
                //
                SwingUtilities.invokeLater(() ->
                        appendMessage("Relaytd has started")
                );
            });
        });
        // Add grids to main panel
        contentPanel.add(springDebug);
        contentPanel.add(Box.createVerticalStrut(50));

        // Put main panel inside scroll pane
        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);


        tabbedPane.addTab("DEBUG", scrollPane);
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
        appendStyledMessage(message, outputDocument, logStyle);
        appendMessageDEBUG(message);
    }

    public void appendStyledMessage(String message, StyledDocument document, SimpleAttributeSet style, boolean hasTimestamp) {
        if (hasTimestamp) {
            message = "[" + timeFormat.format(new Date()) + "] " + message;
        }
        String finalMessage = message;
        SwingUtilities.invokeLater(() -> {
            try {
                int start = document.getLength();
                document.insertString(
                        start,
                        finalMessage + "\n",
                        style
                );
                document.setParagraphAttributes(
                        start,
                        finalMessage.length(),
                        style,
                        false
                );
            } catch (BadLocationException e) {
                e.printStackTrace();
            }
        });
    }

    public void appendStyledMessage(String message, StyledDocument document, SimpleAttributeSet style) {
        appendStyledMessage(message, document, style, true);
    }

    /**
     * Output message to DEBUG Panel
     *
     * @param message
     */
    public void appendMessageDEBUG(String message) {
        appendStyledMessage(message, outputDebugDocument, logStyle);
    }

    public void appendTitle(String title) {
        appendStyledMessage(title, outputDocument, titleStyle, false);
        appendDebugTitle(title);
    }

    public void appendDebugTitle(String title) {
        appendStyledMessage(title, outputDebugDocument, titleStyle, false);
    }

}