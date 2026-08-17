package me.stky.backwork.service;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import me.stky.backwork.model.Task;
import org.bson.BsonValue;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;

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

    private TaskService() {
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
        return collection.find()
                .into(new ArrayList<>())
                .stream()
                .map(Task::fromDocument)
                .toList();
    }

    public BsonValue removeTask(Task task) {
        var result = collection.insertOne(task.toDocument());
        if (result.wasAcknowledged()) {
            return result.getInsertedId();
        } else {
            throw new RuntimeException("Failed to insert task");
        }
    }
}
