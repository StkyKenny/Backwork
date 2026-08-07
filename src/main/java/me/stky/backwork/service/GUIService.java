package me.stky.backwork.service;


import me.stky.backwork.gui.MainFrame;
import org.springframework.stereotype.Service;

import javax.swing.*;

@Service
public class GUIService {


    MainFrame mainFrame;

    public GUIService() {
    }

    public MainFrame getMainFrame() {
        if (mainFrame == null) {
            mainFrame = new MainFrame();
        }
        return mainFrame;
    }

    public void appendMessage(String msg) {
        if (mainFrame != null) {
            SwingUtilities.invokeLater(() ->
                    mainFrame.appendMessage(msg)
            );
        }
    }

    public void appendMessage(String msg, boolean isDebug) {
        if (mainFrame != null) {
            SwingUtilities.invokeLater(() -> {
                        if (isDebug) {
                            mainFrame.appendMessageDEBUG(msg);
                        } else {
                            mainFrame.appendMessage(msg);
                        }
                    }
            );
        }
    }

    public void appendTitle(String msg) {
        if (mainFrame != null) {
            SwingUtilities.invokeLater(() ->
                    mainFrame.appendTitle(msg)
            );
        }
    }
}
