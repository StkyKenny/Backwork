package me.stky.backwork.gui;

import lombok.extern.slf4j.Slf4j;
import me.stky.backwork.BackworkApplication;
import me.stky.backwork.private_config.PrivateConfig;
import me.stky.backwork.service.GUIService;
import me.stky.backwork.service.Helper;
import me.stky.backwork.service.SfxService;
import me.stky.backwork.service.TaskService;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.stream.Collectors;

import static me.stky.backwork.gui.GuiConfig.*;

@Slf4j
public class MainFrame extends JFrame {

    private final int serverPort = 8187;
    private final String appTitle = "STKY | Backwork";
    private ConfigurableApplicationContext context;
    private TrayIcon trayIcon;


    public SfxService sfxService;

    private final JTextPane output = new JTextPane();
    private final JTextPane outputDebug = new JTextPane();
    private final JTabbedPane tabbedPane = new JTabbedPane();
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    int ScreenWidth = screenSize.width;
    int ScreenHeight = screenSize.height;
    StyledDocument outputDocument = output.getStyledDocument();
    StyledDocument outputDebugDocument = outputDebug.getStyledDocument();

    public int homePanelIndex = 0;

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
        return (context != null && context.isRunning());
    }


    public MainFrame() {
        initStyles();
        sfxService = new SfxService(this);

        setTitle(appTitle);
        setSize(GUI_WIDTH, (int) (ScreenHeight * 0.8));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);


        JButton buttonTest = new JButton("Test Spring");

        outputDebug.setEditable(false);
        output.setEditable(false);

        buttonTest.addActionListener(_ -> {
            String result = "test";
            this.appendTitle(result);
            showDesktopNotification(result);
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

        ButtonsSetPanel springDebug = initSpringDebugPanel();
        ButtonsSetPanel otherApps = new ButtonsSetPanel(this, 0, GUI_WIDTH / (BUTTON_WIDTH + H_GAP), H_GAP, V_GAP);
        otherApps.addShortcut("Relaytd", PrivateConfig.RELAYTD_LAUNCH_PATH);
        otherApps.addShortcut("Rclone", PrivateConfig.RCLONE_LAUNCH_PATH);

        otherApps.addButton("Clear Ollama chat history", _ -> {
            // My custom setup has a way to destroy files, so I don't use the os delete files
            appendMessage("Clearing Ollama chat history");
            try {
                var datetime = new SimpleDateFormat("yyyyMMdd-HHmmss");

                var newPath = PrivateConfig.DELETION_PATH + "\\" + datetime.format(new Date()) + "-db.sqlite";
                if (Files.exists(Path.of(newPath))) {
                    newPath += Helper.rdnName();
                }

                try {
                    Path temp = Files.move
                            (Paths.get(PrivateConfig.OLLAMA_CHAT_HISTORY_PATH),
                                    Paths.get(newPath),
                                    StandardCopyOption.REPLACE_EXISTING);
                    appendMessageDEBUG("File renamed and moved successfully");
                } catch (IOException e) {
                    appendMessage("Failed to move the file");
                }

            } catch (Exception e) {
                appendMessage("ERROR : Make sure Ollama isn't running");
                log.error(e.getMessage());
            }
        });
        otherApps.addButton("completed", _ -> {

            TaskService taskService = TaskService.getInstance();
            var a = taskService.getTaskSortedByDeadline().stream().map(Object::toString).collect(Collectors.joining("\n"));
            appendMessageDEBUG(a);
        });

        // Add grids to main panel
        contentPanel.add(springDebug);
        contentPanel.add(Box.createVerticalStrut(50));
        contentPanel.add(otherApps);

        // Put main panel inside scroll pane
        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        var taskPanel = new TaskPanel(this);
        taskPanel.refreshTasks();
        tabbedPane.addTab("Tasks", taskPanel);
        tabbedPane.addTab("DEBUG", scrollPane);
        tabbedPane.addTab("Home", homePanel);
        tabbedPane.addTab("Logs", logsPanel);
        tabbedPane.addTab("Settings", settingsPanel);
        add(tabbedPane, BorderLayout.CENTER);

        this.homePanelIndex = tabbedPane.indexOfComponent(homePanel);
        tabbedPane.addChangeListener(e -> {
            if (tabbedPane.getSelectedIndex() == homePanelIndex) {
                tabbedPane.setBackgroundAt(homePanelIndex, null);
            }
        });


        setupTray();

    }

    private void setupTray() {
        if (!SystemTray.isSupported()) {
            appendMessage("System tray not supported");
            return;
        }
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                setVisible(false);
            }
        });

        SystemTray tray = SystemTray.getSystemTray();
        Image image = null;
        try {
            image = ImageIO.read(getClass().getResource("/icon.jpg"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        PopupMenu popup = new PopupMenu();
        MenuItem openItem = new MenuItem("Open");
        openItem.addActionListener(e -> {
            setVisible(true);
            setExtendedState(JFrame.NORMAL);
            toFront();
        });
        MenuItem exitItem = new MenuItem("Exit");
        exitItem.addActionListener(e -> {
            tray.remove(trayIcon);
            appendMessageDEBUG("Closing from tray");

            System.exit(0);
        });

        popup.add(openItem);
        popup.addSeparator();
        popup.add(exitItem);

        trayIcon = new TrayIcon(image, appTitle, popup);
        trayIcon.setImageAutoSize(true);

        trayIcon.addActionListener(e -> {
            setVisible(true);
            setExtendedState(JFrame.NORMAL);
            toFront();
        });

        try {
            tray.add(trayIcon);
        } catch (AWTException e) {
            System.out.println("TrayIcon could not be added.");
        }
    }

    /**
     * Output message to home tab and Debug Tab
     * Also has timestamp
     * Those are informations user should see
     *
     * @param message Message to be displayed
     */
    public void appendMessage(String message) {
        appendStyledMessage(message, outputDocument, logStyle);
        appendMessageDEBUG(message);
        tabbedPane.setBackgroundAt(homePanelIndex, Color.YELLOW);
        showDesktopNotification(message);
    }

    public void appendStyledMessage(String message, StyledDocument document, SimpleAttributeSet style, boolean hasTimestamp) {
        if (document.equals(outputDebugDocument)) {
            log.info("[GUI] - {}", message);
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

    /**
     * Construct a set of buttons for debug/test purpose
     *
     * @return
     */
    private ButtonsSetPanel initSpringDebugPanel() {
        var springDebug = new ButtonsSetPanel(this, 0, GUI_WIDTH / (BUTTON_WIDTH + H_GAP), H_GAP, V_GAP);
        springDebug.addButton("Start Spring", _ -> {
            if (isSpringRunning()) {
                appendMessage("Spring is already running");
                return;
            }
            appendMessageDEBUG("Spring starting ...");
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

        springDebug.addButton("logger test", _ -> {
            appendMessage("Sent test logs");
            log.trace("Very detailed information");
            log.debug("Debug information");
            log.info("Normal application event");
            log.warn("Something unexpected happened");
            log.error("Something failed");
        });
        springDebug.addButton("Message", _ -> appendMessage("Test message"));
        springDebug.addButton("Debug message", _ -> appendMessageDEBUG("Test message (DEBUG only)"));
        return springDebug;
    }


    public void playSound(String sfxFilepath) {
        try {
            sfxService.playSound(sfxFilepath);
        } catch (Exception e) {
            appendMessageDEBUG("Error trying to play SFX :" + sfxFilepath);
        }
    }


    /**
     * This is adapted from stackOverflow
     * https://stackoverflow.com/questions/34490218/how-to-make-a-windows-notification-in-java
     *
     * @param message
     */
    private void showDesktopNotification(String message) {
        if (SystemTray.isSupported()) {
            SystemTray tray = SystemTray.getSystemTray();

            Image image = Toolkit.getDefaultToolkit().createImage("icon.png");

            TrayIcon trayIcon = new TrayIcon(image, "Tray Demo");
            trayIcon.setImageAutoSize(true);
            trayIcon.setToolTip("System tray icon demo");
            try {
                tray.add(trayIcon);
            } catch (AWTException e) {
                throw new RuntimeException(e);
            }

            trayIcon.displayMessage("Backwork", message, TrayIcon.MessageType.INFO);
        }

    }
}