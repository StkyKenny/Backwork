package me.stky.backwork;

import me.stky.backwork.gui.MainFrame;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

import javax.swing.*;


@SpringBootApplication
@EnableScheduling
@EnableMongoAuditing
public class BackworkApplication extends JFrame {

    public static void main(String[] args) {
        //SpringApplication.run(BackworkApplication.class, args);

        ConfigurableApplicationContext context =
                SpringApplication.run(BackworkApplication.class, args);

        System.out.println("http://localhost:8187/swagger-ui/index.html");
        SwingUtilities.invokeLater(() -> {
            MainFrame frame = context.getBean(MainFrame.class);
            frame.setVisible(true);
        });
    }

}
