package me.stky.backwork.model.routine;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import me.stky.backwork.model.IntervalUnit;
import org.bson.Document;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Routine {
    private String name;
    private String description;
    private String targetExecutablePath;
    private Instant lastExecuted;
    private Instant nextScheduledDate;
    private Integer interval;
    private IntervalUnit intervalUnit;
    private final List<Date> executedDates = new ArrayList<>() {
        @Override
        public String toString() {
            return this.stream()
                    .map(d -> new SimpleDateFormat("yy/MM/dd").format(d))
                    .toList().toString();
        }
    };

    public static final String KEY_NAME = "name";
    public static final String KEY_DESCRIPTION = "description";
    public static final String KEY_TARGET_EXECUTABLE_PATH = "targetExecutablePath";
    public static final String KEY_LAST_EXECUTED = "lastExecuted";
    public static final String KEY_NEXT_SCHEDULED_DATE = "nextScheduledDate";
    public static final String KEY_INTERVAL = "interval";
    public static final String KEY_INTERVAL_UNIT = "intervalUnit";
    public static final String KEY_EXECUTED_DATES = "completedDates";


    public Document toDocument() {
        return new Document()
                .append(KEY_NAME, name)
                .append(KEY_DESCRIPTION, description)
                .append(KEY_TARGET_EXECUTABLE_PATH, targetExecutablePath)
                .append(KEY_LAST_EXECUTED, lastExecuted)
                .append(KEY_NEXT_SCHEDULED_DATE, nextScheduledDate)
                .append(KEY_INTERVAL, interval)
                .append(KEY_INTERVAL_UNIT, intervalUnit)
                .append(KEY_EXECUTED_DATES, executedDates);
    }

    public static Routine fromDocument(Document document) {
        var routine = new Routine();

        routine.setName(document.getString(KEY_NAME));
        routine.setDescription(document.getString(KEY_DESCRIPTION));
        routine.setTargetExecutablePath(document.getString(KEY_TARGET_EXECUTABLE_PATH));

        Date lastExecuted = document.get(KEY_LAST_EXECUTED, Date.class);
        routine.setLastExecuted(lastExecuted == null ? null : lastExecuted.toInstant());
        routine.setNextScheduledDate(document.get(KEY_NEXT_SCHEDULED_DATE, Date.class).toInstant());

        routine.setInterval(document.getInteger(KEY_INTERVAL));
        routine.setIntervalUnit(IntervalUnit.valueOf(document.getString(KEY_INTERVAL_UNIT)));
        routine.executedDates.addAll(document.getList(KEY_EXECUTED_DATES, Date.class));

        return routine;
    }

    public Routine completeOnce() {
        LocalDateTime now = LocalDateTime.now();
        lastExecuted = now.atZone(ZoneId.systemDefault()).toInstant();

        executedDates.add(Date.from(lastExecuted));
        nextScheduledDate = computeNextDate(lastExecuted);
        return this;

    }

    public Instant computeNextDate(Instant currentDate) {
        ZoneId zone = ZoneId.systemDefault();
        var dateSchedule = nextScheduledDate;
        while (dateSchedule.isBefore(Instant.now())) {
            switch (intervalUnit) {
                case DAYS -> dateSchedule = nextScheduledDate.plus(Duration.ofDays(interval));
                case MONTHS -> dateSchedule = nextScheduledDate.atZone(zone).plusMonths(interval).toInstant();
            }
        }
        return dateSchedule;
    }

    public boolean wasExecuted() {
        return (nextScheduledDate.isAfter(Instant.now()));
    }
}
