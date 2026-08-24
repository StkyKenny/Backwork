package me.stky.backwork.service;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import com.mongodb.client.model.Updates;
import com.mongodb.client.result.UpdateResult;
import me.stky.backwork.model.Task;
import me.stky.backwork.model.TaskStatus;
import org.bson.BsonValue;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static com.mongodb.client.model.Sorts.descending;

/**
 * For the GUI to CRUD with MongoDB
 * Given the Swing app isn't always with in a Spring context, this service is used to interact with MongoDB in a low-level.
 *
 * @author stky
 */
public class TaskService {

    private final String connectionString = "mongodb://localhost:27017/";
    private final MongoClient mongoClient = MongoClients.create(connectionString);
    private final MongoDatabase database = mongoClient.getDatabase("backwork_db");
    private final MongoCollection<Document> collection = database.getCollection("tasks");


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
        var now = Instant.now();
        List<Task> updatedTasks = new ArrayList<>();
        for (Task task : tasks) {
            var recurringInfo = task.getRecurringTaskInfo();
            if (recurringInfo != null && now.isAfter(recurringInfo.getFirstStartedDate())) {
                task.setStatus(TaskStatus.TODO);

                updatedTasks.add(resetRecurringTask(task));
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
        var result = collection.updateOne(task.toDocument(), new Document("$set", task.toDocument()));
        if (result.wasAcknowledged() && result.getModifiedCount() == 1) {
            return result.getUpsertedId();
        } else {
            throw new RuntimeException("Failed to update task");
        }
    }

    public List<Task> getTasks() {
        return collection.find().sort(descending("createdDate"))
                .into(new ArrayList<>())
                .stream()
                .map(Task::fromDocument)
                .toList();
    }

    public Integer removeTask(Task task) {
        var result = collection.deleteOne(task.toDocument());
        if (result.wasAcknowledged() && result.getDeletedCount() == 1) {
            return 1;
        } else {
            throw new RuntimeException("Failed to remove task");
        }
    }

    public Task completeTask(Task task) {
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
            throw new RuntimeException("Failed to complete task");
        }
        return collection.find(getTaskIDFilter(task)).map(Task::fromDocument).first();
    }

    private Task resetRecurringTask(Task task) {
        var result = collection.updateOne(
                getTaskIDFilter(task),
                Updates.set("status", TaskStatus.TODO.name())
        );
        if (!result.wasAcknowledged()) {
            throw new RuntimeException("Failed to complete task");
        }
        return collection.find(getTaskIDFilter(task)).map(Task::fromDocument).first();
    }

    private Bson getTaskIDFilter(Task task) {
        return Filters.and(
                Filters.eq("name", task.getName()),
                Filters.eq("createdDate", task.getCreatedDate())
        );
    }
}
