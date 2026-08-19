package me.stky.backwork.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.BsonValue;
import org.bson.Document;

import java.time.Instant;
import java.util.Date;
import java.util.List;


@Data
@AllArgsConstructor
public class Task {

    private BsonValue _id;
    private String name;
    private String description;
    private List<String> tags;
    private TaskStatus status;
    private Instant createdDate;
    private Instant updatedDate;
    private RecurringTaskInfo recurringTaskInfo; // Embedded data

    public Task() {

    }

    public Task(String name, String description) {
        this(name, description, List.of());
    }

    public Task(String name, String description, List<String> tags) {
        this.name = name;
        this.description = description;
        this.tags = tags;
        this.createdDate = Instant.now();
        this.updatedDate = Instant.now();
        this.status = TaskStatus.TODO;
    }

    public Document toDocument() {
        var doc = new Document().append("name", name)
                .append("description", description)
                .append("tags", tags)
                .append("createdDate", createdDate)
                .append("updatedDate", updatedDate)
                .append("status", status);
        if (recurringTaskInfo != null) {
            doc.append("recurringTaskInfo", recurringTaskInfo.toDocument());
        }
        return doc;
    }


    public static Task fromDocument(Document document) {
        var task = new Task();
        task.setName(document.getString("name"));
        task.setDescription(document.getString("description"));
        task.setStatus(TaskStatus.valueOf(document.getString("status")));
        task.setCreatedDate(document.get("createdDate", Date.class).toInstant());
        task.setUpdatedDate(document.get("updatedDate", Date.class).toInstant());

        var recurringTaskInfoDoc = document.get("recurringTaskInfo", Document.class);
        if (recurringTaskInfoDoc != null) {
            task.setRecurringTaskInfo(
                    RecurringTaskInfo.fromDocument(recurringTaskInfoDoc)
            );
        }


        return task;
    }

}

@Data
@NoArgsConstructor
class RecurringTaskInfo {
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
