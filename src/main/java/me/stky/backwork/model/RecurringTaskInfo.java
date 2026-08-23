package me.stky.backwork.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.Document;

import java.time.Instant;
import java.util.Date;

@Data
@NoArgsConstructor
public class RecurringTaskInfo {
    private Integer interval;
    private IntervalUnit intervalUnit;
    private Instant firstStartedDate;
    private Instant lastCompletedDate;
    private Instant nextScheduledDate;
    private Integer completionCount;

    private static final String KEY_INTERVAL = "interval";
    private static final String KEY_INTERVAL_UNIT = "intervalUnit";
    private static final String KEY_FIRST_STARTED_DATE = "firstStartedDate";
    private static final String KEY_LAST_COMPLETED_DATE = "lastCompletedDate";
    private static final String KEY_NEXT_SCHEDULED_DATE = "nextScheduledDate";
    private static final String KEY_COMPLETIONN_COUNT = "completionCount";

    public RecurringTaskInfo(Integer interval, IntervalUnit intervalUnit, Instant firstStartedDate) {
        this.interval = interval;
        this.intervalUnit = intervalUnit;
        this.firstStartedDate = firstStartedDate;
        this.lastCompletedDate = null;
        this.nextScheduledDate = null;
        this.completionCount = 0;

    }

    public Document toDocument() {
        return new Document().append(KEY_INTERVAL, interval)
                .append(KEY_INTERVAL_UNIT, intervalUnit)
                .append(KEY_FIRST_STARTED_DATE, firstStartedDate)
                .append(KEY_LAST_COMPLETED_DATE, lastCompletedDate)
                .append(KEY_NEXT_SCHEDULED_DATE, nextScheduledDate)
                .append(KEY_COMPLETIONN_COUNT, completionCount);
    }

    public static RecurringTaskInfo fromDocument(Document document) {
        var recurringTaskInfo = new RecurringTaskInfo();
        recurringTaskInfo.setInterval(document.getInteger(KEY_INTERVAL));
        recurringTaskInfo.setIntervalUnit(document.get(KEY_INTERVAL_UNIT, IntervalUnit.class));
        recurringTaskInfo.setFirstStartedDate(document.get(KEY_FIRST_STARTED_DATE, Date.class).toInstant());
        recurringTaskInfo.setLastCompletedDate(document.get(KEY_LAST_COMPLETED_DATE, Date.class).toInstant());
        recurringTaskInfo.setNextScheduledDate(document.get(KEY_NEXT_SCHEDULED_DATE, Date.class).toInstant());
        recurringTaskInfo.setCompletionCount(document.getInteger(KEY_COMPLETIONN_COUNT));

        return recurringTaskInfo;
    }
}
