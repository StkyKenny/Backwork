package me.stky.backwork.controller;

import me.stky.backwork.gui.MainFrame;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;

@Component
public class ScheduleTasks {

    // private static final Logger log = (Logger) LoggerFactory.getLogger(ScheduleTasks.class);

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

    @Autowired
    MainFrame MainFrame;

    @Scheduled(fixedRate = 10000) // Every 10 seconds
    public void reportCurrentTime() {
        var msg = "The time is now " + dateFormat.format(new Date());
        System.out.println(msg);
        //log.info("The time is now "+ dateFormat.format(new Date()));
        MainFrame.appendMessage(msg);
    }
}