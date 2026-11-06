package me.stky.backwork;

import me.stky.backwork.gui.GuiReadyEvent;
import me.stky.backwork.service.GUIService;
import me.stky.backwork.service.TaskGUIService;
import me.stky.backwork.service.ZipService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.stream.Stream;

@Component
public class ScheduleTasks {

    private static final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat(" yyyy / MM / dd ");

    @Autowired
    GUIService guiService;
    @Autowired
    TaskGUIService taskGUIService;

    /**
     * Run once on startup
     */
    @EventListener
    public void onGuiReady(GuiReadyEvent event) {
        reportCurrentDay();
        reportCurrentTime();
        cleanupOldLogs();
        // Catch-up
    }

    @Scheduled(fixedRate = 1000 * 60 * 10) // Every 10 min
    public void reportCurrentTime() {
        var msg = "The time is now " + timeFormat.format(new Date());
        guiService.appendMessage(msg, true);
    }

    @Scheduled(cron = "0 0 0 * * *")// Every day at 0 am
    public void reportCurrentDay() {
        var msg = "______________[" + dateFormat.format(new Date()) + "]______________";
        guiService.appendTitle(msg);
    }

    @Scheduled(cron = "0 0 * * * *") // Every hour at minute 0
    public void triggerEachHour() {
        taskGUIService.refreshTasks();
    }


    /**
     * Scheduled task to clean up old log files by zipping older directories
     * Very non-flexible, because it depends on how logback is setup
     */
    @Scheduled(cron = "0 0 0 1 * ?") // Run on the 1st day of every month at 00:00
    public void cleanupOldLogs() {

        Path logDir = Paths.get("../backwork_logs");

        if (!Files.exists(logDir) || !Files.isDirectory(logDir)) {
            guiService.appendMessage("Log directory not found or is not a directory: " + logDir);
            return;
        }

        ZonedDateTime now = ZonedDateTime.now();

        int cutoffMonth = 1;
        int twoMonthsAgo = now.minusMonths(cutoffMonth).getMonthValue();
        int yearTwoMonthsAgo = now.minusMonths(cutoffMonth).getYear();

        try (Stream<Path> paths = Files.walk(logDir)) {
            paths
                    .filter(Files::isDirectory)
                    .filter(path -> !path.equals(logDir))
                    .filter(path -> {

                        var pathParts = path.toUri().toString().split("\\.\\.")[1].split("/");
                        if (pathParts.length < 4) {
                            int year = Integer.parseInt(pathParts[2]);
                            if (year < yearTwoMonthsAgo) {
                                return true;
                            }
                        } else {
                            int month = Integer.parseInt(pathParts[3]);
                            if (month < twoMonthsAgo) {
                                return true;
                            }
                        }
                        return false;
                    })
                    .forEach(dirPath -> {
                        try {
                            Path relativePath = logDir.relativize(dirPath);
                            String fullDirName = relativePath.toString().replace("\\", "/");

                            String zipFileName = fullDirName + "_archive.zip";
                            Path zipFilePath = logDir.resolve(zipFileName);

                            guiService.appendMessage("Found old log directory: " + fullDirName + ". Zipping to " + zipFileName);

                            ZipService.zipFolder(dirPath, zipFilePath);
                            try (var pathsToDelete = Files.walk(dirPath)) {
                                pathsToDelete
                                        .sorted(java.util.Comparator.reverseOrder())
                                        .map(Path::toFile)
                                        .forEach(file -> {
                                            if (!file.delete()) {
                                                guiService.appendMessageDEBUG("Failed to delete deleted: " + file);

                                            } else {
                                                guiService.appendMessageDEBUG("Successfully archived and deleted: " + file);
                                            }
                                        });
                            }


                        } catch (IOException e) {
                            guiService.appendMessage("Error processing directory " + dirPath.getFileName() + ": " + e.getMessage());
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    });
        } catch (IOException e) {
            guiService.appendMessage("Failed to list log directory: " + e.getMessage());
        }
    }

}