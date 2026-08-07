package me.stky.backwork;

import me.stky.backwork.gui.GuiReadyEvent;
import me.stky.backwork.gui.MainFrame;
import me.stky.backwork.service.GUIService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

import javax.swing.*;
import java.awt.*;


@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
@EnableScheduling
@EnableMongoAuditing
public class BackworkApplication extends JPanel {

    public static void main(String[] args) {
        //SpringApplication.run(BackworkApplication.class, args);

        ConfigurableApplicationContext context =
                SpringApplication.run(BackworkApplication.class, args);
        System.out.println("http://localhost:8187/swagger-ui/index.html");

        if (!GraphicsEnvironment.isHeadless()) {
            SwingUtilities.invokeLater(() -> {

                MainFrame frame = context.getBean(GUIService.class).getMainFrame();
                frame.setVisible(true);

                context.publishEvent(new GuiReadyEvent());
            });
        }
    }

}
