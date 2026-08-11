package me.stky.backwork.controller;

import me.stky.backwork.gui.GuiReadyEvent;
import me.stky.backwork.service.GUIService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;

@Component
public class ScheduleTasks {

    private static final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat(" yyyy / MM / dd ");

    @Autowired
    GUIService guiService;

    /**
     * Run once on startup
     */
    @EventListener
    public void onGuiReady(GuiReadyEvent event) {
        reportCurrentDay();
    }

    @Scheduled(fixedRate = 1000 * 10 * 1) // Every minute
    public void reportCurrentTime() {
        var msg = "The time is now " + timeFormat.format(new Date());
        guiService.appendMessage(msg, true);
    }

    @Scheduled(cron = "0 0 0 * * *")// Every day at 0 am
    public void reportCurrentDay() {
        var msg = "______________" + dateFormat.format(new Date()) + "______________";
        guiService.appendTitle(msg);
    }
}