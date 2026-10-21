package me.stky.backwork.service;

import lombok.extern.slf4j.Slf4j;
import me.stky.backwork.model.IntervalUnit;
import me.stky.backwork.model.RecurringTaskInfo;
import me.stky.backwork.model.Task;

import javax.swing.*;
import java.awt.*;
import java.time.Duration;

@Slf4j
public class TaskGUIService {

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
            double t = Math.min(hours / gradientRange, 1.0);

            // 0 -   100  -  180 -> 255 - 0 - 0
            int red = Math.clamp(Math.round(255 * t), 0, 255);
            int green = Math.clamp(100 - (int) Math.round(100 * t), 0, 255);
            int blue = Math.clamp(180 - (int) Math.round(180 * t), 0, 255);

            label.setForeground(new Color(red, green, blue));

            int size = (int) Math.round(maxSize - t * (maxSize - minSize));
            label.setFont(new Font(baseFont.getName(), Font.BOLD, size));
        } catch (Exception e) {
            return;
        }

    }
}
