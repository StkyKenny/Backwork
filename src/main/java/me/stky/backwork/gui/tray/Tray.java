package me.stky.backwork.gui.tray;

import me.stky.backwork.gui.MainFrame;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;

public class Tray {

    public static void setupTray(MainFrame mainFrame) {
        if (!SystemTray.isSupported()) {
            mainFrame.appendMessage("System tray not supported");
            return;
        }

        mainFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        mainFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                mainFrame.setVisible(false);
            }
        });

        SystemTray tray = SystemTray.getSystemTray();
        Image image = null;
        try {
            image = ImageIO.read(mainFrame.getClass().getResource("/icon.jpg"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        PopupMenu popup = new PopupMenu();


        TrayIcon trayIcon = new TrayIcon(image, mainFrame.getTitle(), popup);
        trayIcon.setImageAutoSize(true);

        trayIcon.addActionListener(e -> {
            mainFrame.setVisible(true);
            mainFrame.setExtendedState(JFrame.NORMAL);
            mainFrame.toFront();
        });

        try {
            tray.add(trayIcon);
        } catch (AWTException e) {
            System.out.println("TrayIcon could not be added.");
        }

        MenuItem openItem = new MenuItem("Open");
        openItem.addActionListener(e -> {
            mainFrame.setVisible(true);
            mainFrame.setExtendedState(JFrame.NORMAL);
            mainFrame.toFront();
        });
        MenuItem exitItem = new MenuItem("Exit");
        exitItem.addActionListener(e -> {
            tray.remove(trayIcon);
            mainFrame.appendMessageDEBUG("Closing from tray");

            System.exit(0);
        });

        popup.add(openItem);
        popup.addSeparator();
        popup.add(exitItem);
    }
}
