package me.stky.backwork.gui;

import lombok.extern.slf4j.Slf4j;
import me.stky.backwork.BackworkApplication;
import me.stky.backwork.private_config.PrivateConfig;
import me.stky.backwork.service.GUIService;
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

@Slf4j
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

    private static final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");

    private void initStyles() {
        StyleConstants.setAlignment(titleStyle, StyleConstants.ALIGN_CENTER);
        StyleConstants.setAlignment(titleStyle, StyleConstants.ALIGN_CENTER);
        StyleConstants.setBold(titleStyle, true);
        StyleConstants.setFontSize(titleStyle, 16);
    }

    private boolean isSpringRunning() {
        if (context != null && context.isRunning()) {
            return true;
        }
        return false;
    }


    public MainFrame() {
        initStyles();

        setTitle("Spring + Swing Test");
        setSize(GUI_WIDTH, (int) (ScreenHeight * 0.8));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JButton buttonTest = new JButton("Test Spring");

        outputDebug.setEditable(false);
        output.setEditable(false);

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
        ButtonsSetPanel settingsPanel = new ButtonsSetPanel(this, 0, GUI_WIDTH / (BUTTON_WIDTH + H_GAP), H_GAP, V_GAP);
        settingsPanel.add(new JLabel("Settings tab"), BorderLayout.CENTER);

        // DEBUG / Action Tab
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));

        // First grid
        ButtonsSetPanel springDebug = new ButtonsSetPanel(this, 0, GUI_WIDTH / (BUTTON_WIDTH + H_GAP), H_GAP, V_GAP);
        springDebug.addButton("Start Spring", _ -> {
            if (isSpringRunning()) {
                appendMessage("Spring is already running");
                return;
            }
            appendMessage("Spring starting ...");
            Thread.startVirtualThread(() -> {
                context = SpringApplication.run(BackworkApplication.class);
                GUIService guiService = context.getBean(GUIService.class);
                guiService.setMainFrame(this);
                context.publishEvent(new GuiReadyEvent());
                SwingUtilities.invokeLater(() ->
                        appendMessage("Spring has started")
                );
            });
        });
        springDebug.addButton("Stop Spring", _ -> {
            if (!isSpringRunning()) {
                appendMessage("Spring wasn't started");
                return;
            }
            appendMessage("Spring closing ...");
            context.close();
            appendMessage("Spring closed");
        });

        springDebug.addButton("Swagger", _ -> {
            if (!isSpringRunning()) {
                appendMessage("Spring wasn't started");
                return;
            }
            var link = "http://localhost:" + serverPort + "/swagger-ui/index.html";
            try {
                Desktop.getDesktop().browse(new URI(link));
            } catch (Exception ex) {
                appendMessage("Failed to open " + link);
            }
        });

        springDebug.addButton("Actuator", _ -> {
            if (!isSpringRunning()) {
                appendMessage("Spring wasn't started");
                return;
            }
            var link = "http://localhost:" + serverPort + "/actuator";
            try {
                Desktop.getDesktop().browse(new URI(link));
            } catch (Exception ex) {
                appendMessage("Failed to open " + link);
            }
        });

        springDebug.addButton("Health", _ -> {
            if (!isSpringRunning()) {
                appendMessage("Spring wasn't started");
                return;
            }
            var link = "http://localhost:" + serverPort + "/actuator/health";
            try {
                Desktop.getDesktop().browse(new URI(link));
            } catch (Exception ex) {
                appendMessage("Failed to open " + link);
            }
        });

        springDebug.addButton("logger", _ -> {
            appendMessage("Sent test logs");
            log.trace("Very detailed information");
            log.debug("Debug information");
            log.info("Normal application event");
            log.warn("Something unexpected happened");
            log.error("Something failed");
        });
        springDebug.addButton("Message", _ -> appendMessage("Test message"));
        springDebug.addButton("Debug message", _ -> appendMessageDEBUG("Test message (DEBUG only)"));

        ButtonsSetPanel otherApps = new ButtonsSetPanel(this, 0, GUI_WIDTH / (BUTTON_WIDTH + H_GAP), H_GAP, V_GAP);
        otherApps.addShortcut("Relaytd", PrivateConfig.RELAYTD_LAUNCH_PATH);
        otherApps.addShortcut("Rclone", PrivateConfig.RCLONE_LAUNCH_PATH);

        // Add grids to main panel
        contentPanel.add(springDebug);
        contentPanel.add(Box.createVerticalStrut(50));
        contentPanel.add(otherApps);

        // Put main panel inside scroll pane
        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);


        tabbedPane.addTab("DEBUG", scrollPane);
        tabbedPane.addTab("Home", homePanel);
        tabbedPane.addTab("Logs", logsPanel);
        tabbedPane.addTab("Settings", settingsPanel);
        tabbedPane.addTab("Tasks", new TaskPanel(this));

        add(tabbedPane, BorderLayout.CENTER);
    }

    /**
     * Output message to home tab and Debug Tab
     * Also has timestamp
     *
     * @param message Message to be displayed
     */
    public void appendMessage(String message) {
        appendStyledMessage(message, outputDocument, logStyle);
        appendMessageDEBUG(message);
    }

    public void appendStyledMessage(String message, StyledDocument document, SimpleAttributeSet style, boolean hasTimestamp) {
        if (document.equals(outputDebugDocument)) {
            log.info("[GUI] - " + message);
        }
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
     * @param message Message to be displayed in the debug panel
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