package me.stky.backwork.service;


import me.stky.backwork.gui.MainFrame;
import org.springframework.stereotype.Service;

import javax.swing.*;

/**
 * This service if for Spring to communicate with Java Swing
 */

@Service
public class GUIService {


    MainFrame mainFrame;

    // MainFrame should be linked via setter because on constructor Spring will try to get from scanning component
    // If MainFrame is a component it will try to init which is causing issue by headless exception in test profile
    public void setMainFrame(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
    }

    public void appendMessage(String msg) {
        if (mainFrame != null) {
            SwingUtilities.invokeLater(() ->
                    {
                        mainFrame.appendMessage(msg);
                        mainFrame.appendMessageDEBUG(msg);
                    }
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
