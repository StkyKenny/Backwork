package me.stky.backwork;

import me.stky.backwork.gui.MainFrame;
import me.stky.backwork.service.GUIService;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

import javax.swing.*;
import java.awt.*;


@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
@EnableScheduling
@EnableMongoAuditing
public class BackworkApplication {

    public static void main(String[] args) {
        if (!GraphicsEnvironment.isHeadless()) {
            SwingUtilities.invokeLater(() -> {

                MainFrame frame = new GUIService().getMainFrame();
                frame.setVisible(true);
            });
        }
    }

}
