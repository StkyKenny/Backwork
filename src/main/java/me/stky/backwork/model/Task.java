package me.stky.backwork.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.bson.Document;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Objects;


@Data
@AllArgsConstructor
public class Task {

    private String name;
    private String description;
    private List<String> tags;
    private TaskStatus status;
    private Instant createdDate;
    private Instant updatedDate;
    private Instant deadline;
    private Boolean isTimeConstrained; // If recurring, this can't be time constrained
    private RecurringTaskInfo recurringTaskInfo; // Embedded data

    private static final String KEY_NAME = "name";
    private static final String KEY_DESCRIPTION = "description";
    private static final String KEY_IS_TIME_CONSTRAINED = "isTimeConstrained";
    private static final String KEY_TAGS = "tags";
    private static final String KEY_CREATED_DATE = "createdDate";
    private static final String KEY_UPDATED_DATE = "updatedDate";
    private static final String KEY_DEADLINE = "deadline";
    private static final String KEY_STATUS = "status";
    private static final String KEY_RECURRING_TASK_INFO = "recurringTaskInfo";

    public Task() {

    }

    public Task(String name, String description, Boolean isTimeConstrained) {
        this(name, description, null, isTimeConstrained);
    }

    public Task(String name, String description, Instant deadline, Boolean isTimeConstrained) {
        this(name, description, isTimeConstrained, List.of(), deadline, null);
    }


    public Task(String name, String description, RecurringTaskInfo recurringTaskInfo) {
        this(name, description, null, recurringTaskInfo);
    }

    public Task(String name, String description, Instant deadline, RecurringTaskInfo recurringTaskInfo) {
        this(name, description, Boolean.FALSE, List.of(), deadline, recurringTaskInfo);
    }

    private Task(String name, String description, Boolean isTimeConstrained, List<String> tags, Instant deadline, RecurringTaskInfo recurringTaskInfo) {
        this.name = name.trim();
        this.description = description.trim();
        this.isTimeConstrained = isTimeConstrained;
        this.tags = tags;
        this.recurringTaskInfo = recurringTaskInfo;
        this.createdDate = Instant.now();
        this.updatedDate = Instant.now();
        this.deadline = deadline;
        this.status = TaskStatus.TODO;
    }

    // Identified by its name and creation date
    public String getID() {
        return name + " | " + createdDate.toString();
    }

    public Document toDocument() {
        var doc = new Document()
                .append(KEY_NAME, name)
                .append(KEY_DESCRIPTION, description)
                .append(KEY_IS_TIME_CONSTRAINED, isTimeConstrained)
                .append(KEY_TAGS, tags)
                .append(KEY_CREATED_DATE, createdDate)
                .append(KEY_UPDATED_DATE, updatedDate)
                .append(KEY_DEADLINE, deadline)
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
        var deadline = document.get(KEY_DEADLINE, Date.class);
        if (deadline != null) {
            task.setDeadline(deadline.toInstant());
        }

        var recurringTaskInfoDoc = document.get(KEY_RECURRING_TASK_INFO, Document.class);

        if (recurringTaskInfoDoc != null) {
            task.setRecurringTaskInfo(
                    RecurringTaskInfo.fromDocument(recurringTaskInfoDoc)
            );
        }


        return task;
    }

    public String displayTimeRemaining() throws IllegalStateException {
        Duration durationRemaining = computeTimeRemaining();
        StringBuilder sb = new StringBuilder();
        String qualifier = " remained";
        String qualifier2 = "";
        if (durationRemaining.isNegative()) {
            durationRemaining = durationRemaining.negated();
            qualifier = " ago";
            qualifier2 = " exceeded";
        }
        var remainingDays = durationRemaining.toDays();
        if (remainingDays > 0) {
            float hourDecimal = (float) durationRemaining.toHours() / 24;
            var hourDecimalString = String.format("%.2f", hourDecimal);
            if (hourDecimal < 0.1) {
                hourDecimalString = "";
            }
            return sb.append(hourDecimalString).append(" days").append(qualifier).toString();
        }
        var remainingHours = durationRemaining.toHours();
        if (remainingHours > 0) {
            return sb.append(remainingHours).append(" hours").append(qualifier).toString();
        } else {
            return sb.append("Under an hour").append(qualifier2).toString();
        }
    }

    /**
     * Get a duration of the time until the deadline
     * Negative value indicates time was exceeded
     *
     * @return
     */
    public Duration computeTimeRemaining() throws IllegalStateException {
        var now = Instant.now();
        if (isTimeConstrained) {
            throw new IllegalStateException("Not yet implemented");
        }
        if (recurringTaskInfo == null) {
            if (deadline == null) {
                throw new IllegalStateException("This Task doesn't have a deadline");
            }
            return Duration.between(now, deadline);
        } else {
            Instant nextScheduledDate = recurringTaskInfo.getNextScheduledDate();
            var deadlineOfScheduledDate = recurringTaskInfo.computeNextDate(nextScheduledDate);
            return Duration.between(now, deadlineOfScheduledDate);
        }
    }
}

