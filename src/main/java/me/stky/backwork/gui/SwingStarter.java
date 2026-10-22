package me.stky.backwork.gui;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import javax.swing.*;

@Component
public class SwingStarter {

    private final MainFrame mainFrame;

    public SwingStarter(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void startGui() {
        SwingUtilities.invokeLater(() -> {
            mainFrame.setVisible(true);
        });
    }
}