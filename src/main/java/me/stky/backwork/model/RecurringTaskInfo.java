package me.stky.backwork.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.Document;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
public class RecurringTaskInfo {
    private Integer interval;
    private IntervalUnit intervalUnit;
    private Instant firstStartedDate;
    private Instant lastCompletedDate;
    private Instant nextScheduledDate;
    private final List<Instant> completedDates = new ArrayList<>();
    private Integer completionCount;

    private static final String KEY_INTERVAL = "interval";
    private static final String KEY_INTERVAL_UNIT = "intervalUnit";
    private static final String KEY_FIRST_STARTED_DATE = "firstStartedDate";
    private static final String KEY_LAST_COMPLETED_DATE = "lastCompletedDate";
    private static final String KEY_NEXT_SCHEDULED_DATE = "nextScheduledDate";
    private static final String KEY_COMPLETION_COUNT = "completionCount";

    public RecurringTaskInfo(Integer interval, IntervalUnit intervalUnit, Instant firstStartedDate) {
        this.interval = interval;
        this.intervalUnit = intervalUnit;
        this.firstStartedDate = firstStartedDate.atZone(ZoneOffset.UTC)
                .toLocalDate()
                .atStartOfDay(ZoneOffset.UTC)
                .toInstant();
        this.lastCompletedDate = null;
        this.nextScheduledDate = firstStartedDate;
        this.completionCount = 0;

    }

    public Document toDocument() {
        return new Document().append(KEY_INTERVAL, interval)
                .append(KEY_INTERVAL_UNIT, intervalUnit)
                .append(KEY_FIRST_STARTED_DATE, firstStartedDate)
                .append(KEY_LAST_COMPLETED_DATE, lastCompletedDate)
                .append(KEY_NEXT_SCHEDULED_DATE, nextScheduledDate)
                .append(KEY_COMPLETION_COUNT, completionCount);
    }

    public static RecurringTaskInfo fromDocument(Document document) {
        var recurringTaskInfo = new RecurringTaskInfo();
        recurringTaskInfo.setInterval(document.getInteger(KEY_INTERVAL));
        recurringTaskInfo.setIntervalUnit(IntervalUnit.valueOf(document.getString(KEY_INTERVAL_UNIT)));

        recurringTaskInfo.setFirstStartedDate(document.get(KEY_FIRST_STARTED_DATE, Date.class).toInstant());
        Date lastCompletedDate = document.get(KEY_LAST_COMPLETED_DATE, Date.class);
        recurringTaskInfo.setLastCompletedDate(lastCompletedDate == null ? null : lastCompletedDate.toInstant());
        Date nextScheduledDate = document.get(KEY_NEXT_SCHEDULED_DATE, Date.class);
        recurringTaskInfo.setNextScheduledDate(nextScheduledDate == null ? null : nextScheduledDate.toInstant());
        recurringTaskInfo.setCompletionCount(document.getInteger(KEY_COMPLETION_COUNT));

        return recurringTaskInfo;
    }

    public void completeOnce() {
        LocalDateTime now = LocalDateTime.now();
        ChronoUnit chronoUnit = switch (intervalUnit) {
            case DAYS -> ChronoUnit.DAYS;
            case MONTHS -> ChronoUnit.MONTHS;
        };

        completionCount += 1;
        lastCompletedDate = now.atZone(ZoneOffset.UTC)
                .toLocalDate()
                .atStartOfDay(ZoneOffset.UTC)
                .toInstant();
        ;
        completedDates.add(lastCompletedDate);
        nextScheduledDate = now.plus(interval, chronoUnit).atZone(ZoneOffset.UTC)
                .toLocalDate()
                .atStartOfDay(ZoneOffset.UTC)
                .toInstant();
        ;
    }
}
