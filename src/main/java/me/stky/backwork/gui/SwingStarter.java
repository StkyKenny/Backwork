package me.stky.backwork.gui;

import me.stky.backwork.service.GUIService;
import me.stky.backwork.service.TaskGUIService;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.awt.*;

@Component
public class SwingStarter {

    private final ApplicationEventPublisher eventPublisher;

    private final MainFrameFactory mainFrameFactory;
    private final GUIService guiService;
    private final TaskGUIService taskGUIService;

    public SwingStarter(
            ApplicationEventPublisher eventPublisher, MainFrameFactory mainFrameFactory, GUIService guiService,
            TaskGUIService taskGUIService) {
        this.eventPublisher = eventPublisher;
        this.mainFrameFactory = mainFrameFactory;
        this.guiService = guiService;
        this.taskGUIService = taskGUIService;


    }

    @EventListener(ApplicationReadyEvent.class)
    public void startGui() {
        // Only run this code if we are in a visual environment
        if (!GraphicsEnvironment.isHeadless()) {
            MainFrame frame = mainFrameFactory.getMainFrame();
            guiService.setMainFrame(frame);
            taskGUIService.setTaskPanel(frame.taskPanel);

            SwingUtilities.invokeLater(() -> {
                frame.setVisible(true);
                eventPublisher.publishEvent(new GuiReadyEvent());
            });
        }
    }
}