package me.stky.backwork.service;

import lombok.extern.slf4j.Slf4j;
import me.stky.backwork.gui.task.TaskPanel;
import me.stky.backwork.model.IntervalUnit;
import me.stky.backwork.model.RecurringTaskInfo;
import me.stky.backwork.model.Task;
import org.springframework.stereotype.Service;

import javax.swing.*;
import java.awt.*;
import java.time.Duration;

@Slf4j
@Service
public class TaskGUIService {
    private TaskPanel taskPanel;

    public void setTaskPanel(TaskPanel taskPanel) {
        this.taskPanel = taskPanel;
    }

    public static void calculateDynamicPriorityFont(JLabel label, Task task) {
        Font baseFont = label.getFont();
        int minSize = baseFont.getSize();
        int maxSize = minSize + 6;

        try {
            Duration durationRemaining = task.computeTimeRemaining();
            if (durationRemaining.isNegative()) {
                label.setFont(new Font(baseFont.getName(), Font.BOLD, maxSize));
                label.setForeground(Color.RED);
                return;
            }
            double hours = durationRemaining.toMillis() / (1000.0 * 60 * 60);

            int gradientRange = 5 * 24; // Default gradient is on 5 days
            RecurringTaskInfo reccuringInfo = task.getRecurringTaskInfo();
            if (reccuringInfo != null) {
                if (reccuringInfo.getIntervalUnit().equals(IntervalUnit.DAYS)) {
                    gradientRange = reccuringInfo.getInterval() * 24;
                }
            }
            double t = Math.clamp(hours / gradientRange, 0.0, 1.0);

            // 0 -   100  -  180 -> 255 - 0 - 0
            int red = (int) Math.round(255 - (255 * t));
            int green = (int) Math.round(100 * t);
            int blue = (int) Math.round(180 * t);

            label.setForeground(new Color(red, green, blue));

            int size = (int) Math.round(maxSize - t * (maxSize - minSize));
            label.setFont(new Font(baseFont.getName(), Font.BOLD, size));
        } catch (Exception e) {
            return;
        }

    }

    /**
     * It will check and update recurring tasks clearing them
     */
    public void refreshTasks() {
        taskPanel.clearTasksFromPanel();
        taskPanel.loadTasks();
    }
}
