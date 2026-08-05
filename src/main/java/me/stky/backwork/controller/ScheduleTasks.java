package me.stky.backwork.controller;

import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;

@Component
public class ScheduleTasks {

   // private static final Logger log = (Logger) LoggerFactory.getLogger(ScheduleTasks.class);

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

    @Scheduled(fixedRate = 10000) // Every 10 seconds
    public void reportCurrentTime() {
        System.out.println("The time is now "+ dateFormat.format(new Date()));
        //log.info("The time is now "+ dateFormat.format(new Date()));
    }
}