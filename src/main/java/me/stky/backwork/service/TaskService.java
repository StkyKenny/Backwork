package me.stky.backwork.service;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import com.mongodb.client.model.Updates;
import me.stky.backwork.model.Task;
import me.stky.backwork.model.TaskStatus;
import org.bson.BsonValue;
import org.bson.Document;

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

    public BsonValue completeTask(Task task) {
        var result = collection.updateOne(
                Filters.and(
                        Filters.eq("status", task.getStatus()),
                        Filters.eq("name", task.getName()),
                        Filters.eq("createdDate", task.getCreatedDate())
                ),
                Updates.set("status", TaskStatus.COMPLETED.name()));
        if (!result.wasAcknowledged()) {
            throw new RuntimeException("Failed to complete task");
        }
        return result.getUpsertedId();
    }
}
