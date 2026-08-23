package me.stky.backwork.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.bson.BsonValue;
import org.bson.Document;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Objects;


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
    private Boolean isTimeConstrained; // If recurring, this can't be time constrained
    private RecurringTaskInfo recurringTaskInfo; // Embedded data

    private static final String KEY_NAME = "name";
    private static final String KEY_DESCRIPTION = "description";
    private static final String KEY_IS_TIME_CONSTRAINED = "isTimeConstrained";
    private static final String KEY_TAGS = "tags";
    private static final String KEY_CREATED_DATE = "createdDate";
    private static final String KEY_UPDATED_DATE = "updatedDate";
    private static final String KEY_STATUS = "status";
    private static final String KEY_RECURRING_TASK_INFO = "recurringTaskInfo";

    public Task() {

    }

    public Task(String name, String description, Boolean isTimeConstrained) {
        this(name, description, isTimeConstrained, List.of(), null);
    }

    public Task(String name, String description, RecurringTaskInfo recurringTaskInfo) {
        this(name, description, Boolean.FALSE, List.of(), recurringTaskInfo);
    }

    private Task(String name, String description, Boolean isTimeConstrained, List<String> tags, RecurringTaskInfo recurringTaskInfo) {
        this.name = name;
        this.description = description;
        this.isTimeConstrained = isTimeConstrained;
        this.tags = tags;
        this.recurringTaskInfo = recurringTaskInfo;
        this.createdDate = Instant.now();
        this.updatedDate = Instant.now();
        this.status = TaskStatus.TODO;
    }

    public Document toDocument() {
        var doc = new Document().append(KEY_NAME, name)
                .append(KEY_DESCRIPTION, description)
                .append(KEY_IS_TIME_CONSTRAINED, isTimeConstrained)
                .append(KEY_TAGS, tags)
                .append(KEY_CREATED_DATE, createdDate)
                .append(KEY_UPDATED_DATE, updatedDate)
                .append(KEY_STATUS, status);
        if (recurringTaskInfo != null) {
            doc.append("recurringTaskInfo", recurringTaskInfo.toDocument());
        }
        return doc;
    }


    public static Task fromDocument(Document document) {
        Objects.requireNonNull(document);
        var task = new Task();
        task.setName(document.getString(KEY_NAME));
        task.setDescription(document.getString(KEY_DESCRIPTION));
        task.setIsTimeConstrained(document.getBoolean(KEY_IS_TIME_CONSTRAINED));
        task.setStatus(TaskStatus.valueOf(document.getString(KEY_STATUS)));
        task.setCreatedDate(document.get(KEY_CREATED_DATE, Date.class).toInstant());
        task.setUpdatedDate(document.get(KEY_UPDATED_DATE, Date.class).toInstant());

        var recurringTaskInfoDoc = document.get(KEY_RECURRING_TASK_INFO, Document.class);

        if (recurringTaskInfoDoc != null) {
            task.setRecurringTaskInfo(
                    RecurringTaskInfo.fromDocument(recurringTaskInfoDoc)
            );
        }


        return task;
    }

}

