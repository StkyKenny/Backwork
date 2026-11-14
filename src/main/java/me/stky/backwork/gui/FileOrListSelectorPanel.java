package me.stky.backwork.gui;

import me.stky.backwork.service.CustomExecutionsService;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class FileOrListSelectorPanel extends JPanel {

    private JComboBox<String> listSelector;
    private JButton fileSelectorButton;
    private JButton run;
    private final Map<String, Runnable> methodRegistry = new HashMap<>();

    CustomExecutionsService customExecutionsService;

    /**
     * Constructor for the FileOrListSelectorPanel.
     *
     * @param listItems The list of String items to populate the dropdown.
     */
    public FileOrListSelectorPanel(CustomExecutionsService customExecutionsService, List<String> listItems) {
        this.customExecutionsService = customExecutionsService;
        setLayout(new BorderLayout(5, 5));
        setBorder(BorderFactory.createTitledBorder("Select File or Custom Action"));

        // 1. List Selector (JComboBox)
        listSelector = new JComboBox<>(listItems.toArray(new String[0]));
        listSelector.setSelectedItem(null);
        listSelector.addActionListener(e -> handleSelectionChange(listSelector.getSelectedItem().toString()));
        listSelector.setEditable(true);
        // 2. File Selector Button
        fileSelectorButton = new JButton("\uD83D\uDCBF");
        fileSelectorButton.addActionListener(e -> handleSelectionChange(selectFile()));


        // Layout setup
        JPanel controlPanel = new JPanel(new BorderLayout(5, 0));
        controlPanel.add(listSelector, BorderLayout.CENTER);
        controlPanel.add(fileSelectorButton, BorderLayout.EAST);

        run = new JButton("run");
        run.addActionListener(_ -> {
            String methodName = getSelectedValue();
            System.out.println(methodName);
            try {
                executeMethodAction(methodName);
            } catch (IllegalArgumentException e) {
                System.out.println("Error: Action not found: ");
            }
        });

        JPanel panel = new JPanel(new GridBagLayout());

        panel.setTransferHandler(new TransferHandler() {

            @Override
            public boolean canImport(TransferSupport support) {
                return support.isDataFlavorSupported(DataFlavor.javaFileListFlavor);
            }

            @Override
            public boolean importData(TransferSupport support) {
                if (!canImport(support)) {
                    return false;
                }

                try {
                    List<File> files = (List<File>) support.getTransferable()
                            .getTransferData(DataFlavor.javaFileListFlavor);

                    for (File file : files) {
                        System.out.println("Dropped file: " + file.getAbsolutePath());
                        handleSelectionChange(file.getAbsolutePath());
                    }

                    return true;

                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }
            }
        });

        JLabel label = new JLabel("Drag and drop Executable here");
        panel.add(label);

        registerActionsFromService(customExecutionsService);
        controlPanel.add(panel, BorderLayout.SOUTH);
        add(controlPanel, BorderLayout.NORTH);
    }

    public void executeMethodAction(String actionName) {
        Runnable action = methodRegistry.get(actionName);
        if (action != null) {
            action.run();
        } else {
            throw new IllegalArgumentException("Unknown action: " + actionName);
        }
    }

    /**
     * Dynamically registers all public methods from a given service class.
     * NOTE: This assumes the methods take no arguments (or only arguments that can be ignored).
     *
     * @param service The service class whose methods should be registered.
     */
    private void registerActionsFromService(Object service) {
        Class<?> serviceClass = service.getClass();

        List<Method> methods = java.util.Arrays.stream(serviceClass.getMethods())
                .filter(method -> method.getParameterCount() == 0) // Filter for methods with no arguments
                .collect(Collectors.toList());

        for (Method method : methods) {
            String methodName = method.getName();

            Runnable action = () -> {
                try {
                    method.invoke(service);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    //mainframe.appendMessage("Error executing method " + methodName + ": " + e.getMessage());
                }
            };

            methodRegistry.put(methodName, action);
        }
        //mainframe.appendMessageDEBUG("Registered " + methods.size() + " methods from " + serviceClass.getSimpleName());
    }


    private void handleSelectionChange(String selectedItem) {
        System.out.println(selectedItem);
        listSelector.setSelectedItem(selectedItem);
    }

    /**
     * Opens a JFileChooser dialog to allow the user to select a file.
     */
    private String selectFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select a File");

        int userSelection = fileChooser.showOpenDialog(this);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            String filePath = selectedFile.getAbsolutePath();
            return filePath;
        }
        return "";
    }

    public String getSelectedValue() {
        return listSelector.getSelectedItem().toString();
    }

}