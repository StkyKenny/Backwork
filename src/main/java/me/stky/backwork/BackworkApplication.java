package me.stky.backwork;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;


@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
@EnableScheduling
@EnableMongoAuditing
public class BackworkApplication {

    public static void main(String[] args) {

        new SpringApplicationBuilder(BackworkApplication.class)
                .headless(false)
                .run(args);
    }

    // OLDER IDEAS
    //ConfigurableApplicationContext context = SpringApplication.run(BackworkApplication.class, args);

        /*SpringApplicationBuilder builder = new SpringApplicationBuilder(BackworkApplication.class);

        builder.headless(false);

        ConfigurableApplicationContext context = builder.run(args);
        SwingUtilities.invokeLater(() -> {

            MainFrame frame = context.getBean(GUIService.class).getMainFrame();
            frame.setVisible(true);

            context.publishEvent(new GuiReadyEvent());
        });*/

}
