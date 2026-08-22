package me.stky.backwork.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.Document;

import java.time.Instant;
import java.util.Date;

@Data
@NoArgsConstructor
public class RecurringTaskInfo {
    private Integer intervalInMinutes;
    private Instant firstStartedDate;
    private Instant lastCompletedDate;
    private Instant nextScheduledDate;
    private Integer completionCount;

    public RecurringTaskInfo(Integer intervalInMinutes, Instant firstStartedDate) {
        this.intervalInMinutes = intervalInMinutes;
        this.firstStartedDate = firstStartedDate;
        this.lastCompletedDate = null;
        this.nextScheduledDate = null;
        this.completionCount = 0;

    }

    public Document toDocument() {
        return new Document().append("intervalInMinutes", intervalInMinutes)
                .append("firstStartedDate", firstStartedDate)
                .append("lastCompletedDate", lastCompletedDate)
                .append("nextScheduledDate", nextScheduledDate)
                .append("completionCount", completionCount);
    }

    public static RecurringTaskInfo fromDocument(Document document) {
        var recurringTaskInfo = new RecurringTaskInfo();
        recurringTaskInfo.setIntervalInMinutes(document.getInteger("intervalInMinutes"));
        recurringTaskInfo.setFirstStartedDate(document.get("firstStartedDate", Date.class).toInstant());
        recurringTaskInfo.setLastCompletedDate(document.get("lastCompletedDate", Date.class).toInstant());
        recurringTaskInfo.setNextScheduledDate(document.get("nextScheduledDate", Date.class).toInstant());
        recurringTaskInfo.setCompletionCount(document.getInteger("completionCount"));

        return recurringTaskInfo;
    }
}
