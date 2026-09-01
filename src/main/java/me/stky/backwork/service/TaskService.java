package me.stky.backwork.service;

import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import com.mongodb.client.model.Updates;
import com.mongodb.client.result.UpdateResult;
import lombok.extern.slf4j.Slf4j;
import me.stky.backwork.model.Task;
import me.stky.backwork.model.TaskStatus;
import org.bson.BsonValue;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import java.sql.SQLException;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import static com.mongodb.client.model.Sorts.descending;

/**
 * For the GUI to CRUD with MongoDB
 * Given the Swing app isn't always with in a Spring context, this service is used to interact with MongoDB in a low-level.
 *
 * @author stky
 */
@Slf4j
public class TaskService {

    private final String connectionString = "mongodb://localhost:27017/";
    private final MongoClient mongoClient = MongoClients.create(connectionString);
    private final MongoDatabase database = mongoClient.getDatabase("backwork_db");
    private final MongoCollection<Document> collection = database.getCollection("tasks");
    private final MongoCollection<Document> collection_archive = database.getCollection("tasks_archive");


    private void createIndexes() {
        collection.createIndex(
                Indexes.compoundIndex(
                        Indexes.ascending("status"),
                        Indexes.ascending("name"),
                        Indexes.ascending("createdDate")
                ),
                new IndexOptions().unique(true)
        );
    }

    private TaskService() {
        //createIndexes(); // run once
    }

    private static final TaskService INSTANCE = new TaskService();

    public static TaskService getInstance() {
        return INSTANCE;
    }

    /**
     * Refresh routine, set status to todo when recurrent task is ready again
     */
    public List<Task> refresh(List<Task> tasks) {
        var now = Instant.now().atZone(ZoneId.systemDefault());
        List<Task> updatedTasks = new ArrayList<>();
        for (Task task : tasks) {
            var recurringInfo = task.getRecurringTaskInfo();
            if (recurringInfo != null) {
                if (now.isAfter(recurringInfo.getNextScheduledDate().atZone(ZoneId.systemDefault()))) {
                    task.setStatus(TaskStatus.TODO);

                    try {
                        updatedTasks.add(resetRecurringTask(task));
                    } catch (SQLException e) {
                        log.error("Error trying to reset this task :{}", task.getID());
                    }

                }
            }
        }
        return updatedTasks;
    }

    public BsonValue addTask(Task task) {
        var result = collection.insertOne(task.toDocument());
        if (result.wasAcknowledged()) {
            return result.getInsertedId();
        } else {
            throw new RuntimeException("Failed to insert task");
        }
    }

    public BsonValue updateTask(Task task) {
        var result = collection.updateOne(getTaskIDFilter(task), new Document("$set", task.toDocument()));
        if (result.wasAcknowledged() && result.getModifiedCount() == 1) {
            return result.getUpsertedId();
        } else {
            throw new RuntimeException("Failed to update task");
        }
    }

    public ObjectId replaceTask(Task task, Task newTask) {
        newTask.setUpdatedDate(Instant.now());
        var result = collection.findOneAndReplace(getTaskIDFilter(task), newTask.toDocument());
        if (result != null) {
            return result.getObjectId("_id");
        } else {
            throw new RuntimeException("Failed to replace task");
        }
    }

    public List<Task> getTasks() {
        return collection.find().sort(descending("createdDate"))
                .into(new ArrayList<>())
                .stream()
                .map(Task::fromDocument)
                .toList();
    }

    public Task deleteTask(Task task) throws SQLException {
        var result = collection.deleteOne(task.toDocument());
        if (result.wasAcknowledged() && result.getDeletedCount() == 1) {
            return task;
        } else {
            throw new SQLException("Failed to remove task");
        }
    }

    public Task completeTask(Task task) throws SQLException {
        UpdateResult result;
        if (task.getRecurringTaskInfo() == null) {
            result = collection.updateOne(
                    getTaskIDFilter(task),
                    Updates.combine(
                            Updates.set("status", TaskStatus.COMPLETED.name()),
                            Updates.set("updatedDate", Instant.now())
                    ));
        } else {
            task.getRecurringTaskInfo().completeOnce();
            result = collection.updateOne(
                    getTaskIDFilter(task),
                    Updates.combine(
                            Updates.set("status", TaskStatus.COMPLETED.name()),
                            Updates.set("updatedDate", Instant.now()),
                            Updates.set("recurringTaskInfo", task.getRecurringTaskInfo().toDocument())
                    ));
        }

        if (!result.wasAcknowledged()) {
            throw new SQLException("Failed to complete task");
        }
        return collection.find(getTaskIDFilter(task)).map(Task::fromDocument).first();
    }

    private Task resetRecurringTask(Task task) throws SQLException {
        var result = collection.updateOne(
                getTaskIDFilter(task),
                Updates.set("status", TaskStatus.TODO.name())
        );
        if (!result.wasAcknowledged()) {
            throw new SQLException("Failed to complete task");
        }
        return collection.find(getTaskIDFilter(task)).map(Task::fromDocument).first();
    }


    /***
     * Will archive the task by moving it to the archive collection and setting it to status ARCHIVED
     * Use Transaction body to garantee atomicity
     * @param task
     */
    public void archiveTask(Task task) throws SQLException {

        final ClientSession clientSession = mongoClient.startSession();

        TransactionBody<String> txnBody = new TransactionBody<>() {
            public String execute() {
                var updatedTaskDocument = collection.findOneAndDelete(getTaskIDFilter(task));
                if (updatedTaskDocument == null) {
                    throw new RuntimeException("Coulnd't find and delete the task to archive");
                }
                Task updatedTask = Task.fromDocument(updatedTaskDocument);
                updatedTask.setStatus(TaskStatus.ARCHIVED);
                updatedTask.setUpdatedDate(Instant.now());
                collection_archive.insertOne(updatedTask.toDocument());

                return "Archived :" + task.getID();
            }
        };

        try {
            clientSession.withTransaction(txnBody);
        } catch (RuntimeException e) {
            throw new SQLException(e.getMessage());
        } finally {
            clientSession.close();
        }
    }

    private Bson getTaskIDFilter(Task task) {
        return Filters.and(
                Filters.eq("name", task.getName()),
                Filters.eq("createdDate", task.getCreatedDate())
        );
    }


}
