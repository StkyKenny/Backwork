package me.stky.backwork;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;


@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
@EnableScheduling
@EnableMongoAuditing
public class BackworkApplication {

    public static void main(String[] args) {
        //SpringApplication.run(BackworkApplication.class, args);

        ConfigurableApplicationContext context =
                SpringApplication.run(BackworkApplication.class, args);

        System.out.println("http://localhost:8187/swagger-ui/index.html");
        /*SwingUtilities.invokeLater(() -> {
            MainFrame frame = context.getBean(MainFrame.class);
            frame.setVisible(true);
        });*/
    }

}
