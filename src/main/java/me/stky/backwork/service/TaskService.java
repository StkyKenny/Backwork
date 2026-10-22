package me.stky.backwork.service;

import com.mongodb.client.*;
import com.mongodb.client.model.*;
import com.mongodb.client.result.UpdateResult;
import lombok.extern.slf4j.Slf4j;
import me.stky.backwork.model.Task;
import me.stky.backwork.model.TaskStatus;
import org.bson.BsonValue;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.mongodb.client.model.Sorts.ascending;
import static com.mongodb.client.model.Sorts.descending;
import static java.util.Arrays.asList;

/**
 * For the GUI to CRUD with MongoDB
 * Given the Swing app isn't always with in a Spring context, this service is used to interact with MongoDB in a low-level.
 * I am using SQLException to indicate error with MongoDB
 *
 * @author stky
 */
@Service
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
    public List<Task> reset(List<Task> tasks) {
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

                } else {
                    updatedTasks.add(task);
                }
            } else {
                updatedTasks.add(task);
            }
        }
        return updatedTasks;
    }


    public BsonValue addTask(Task task) throws SQLException {
        var find = collection.find(getTaskIDFilter(task)).first();
        if (find != null) {
            throw new IllegalArgumentException("The Task " + task.getID() + " already exists in DB");
        }
        var result = collection.insertOne(task.toDocument());
        if (result.wasAcknowledged()) {
            return result.getInsertedId();
        } else {
            throw new SQLException("Failed to insert task");
        }
    }

    // Not yet found a correct purpose when compared to replaceTask()
    public BsonValue updateTask(Task task) throws SQLException {
        var result = collection.updateOne(getTaskIDFilter(task), new Document("$set", task.toDocument()));
        if (result.wasAcknowledged() && result.getModifiedCount() == 1) {
            return result.getUpsertedId();
        } else {
            throw new SQLException("Failed to update task");
        }
    }

    /**
     * @return The objectID that was updated
     */
    public ObjectId replaceTask(Task task, Task newTask) throws SQLException {
        newTask.setUpdatedDate(Instant.now());
        var result = collection.findOneAndReplace(getTaskIDFilter(task), newTask.toDocument());
        if (result != null) {
            return result.getObjectId("_id");
        } else {
            throw new SQLException("Failed to replace task");
        }
    }

    public List<Task> getTasks() {
        return collection.find().sort(descending("createdDate"))
                .into(new ArrayList<>())
                .stream()
                .map(Task::fromDocument)
                .toList();
    }

    /**
     * Delete the Task from the DB
     *
     * @param task The Task to delete
     * @return The deleted Task
     * @throws SQLException if the Task wasn't deleted
     */
    public Task deleteTask(Task task) throws SQLException {
        var result = collection.deleteOne(getTaskIDFilter(task));
        if (result.wasAcknowledged() && result.getDeletedCount() == 1) {
            return task;
        } else {
            throw new SQLException("Failed to remove task");
        }
    }

    /**
     * Set the Task status to Completed
     *
     * @param task The Task to complete
     * @return The updated Task from the Database
     * @throws SQLException if the transaction wasn't acknowledged
     */
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
     * Use Transaction body to guarantee atomicity
     * @param task The Task to archive
     */
    public void archiveTask(Task task) throws SQLException {

        final ClientSession clientSession = mongoClient.startSession();

        TransactionBody<String> txnBody = new TransactionBody<>() {
            public String execute() {
                var updatedTaskDocument = collection.findOneAndDelete(getTaskIDFilter(task));
                if (updatedTaskDocument == null) {
                    throw new RuntimeException("Couldn't find and delete the task to archive");
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

    /**
     * Create the filter used for finding the correct Task
     * Currently defined by the name of the task and its creation date
     *
     * @param task the task to search
     * @return The MongoDB Filter
     */
    private Bson getTaskIDFilter(Task task) {
        return Filters.and(
                Filters.eq("name", task.getName()),
                Filters.eq("createdDate", task.getCreatedDate())
        );
    }

    /**
     * Get Task Sorted by nearest upcoming deadline to furthest, then by oldest createdDate
     *
     * @return
     */
    public List<Task> getTaskSortedByDeadline() {
        Bson addFieldsStage = Aggregates.addFields(
                new Field<>(
                        "_deadlineOrder",
                        new Document("$cond",
                                Arrays.asList(
                                        new Document("$ifNull", Arrays.asList("$deadline", false)),
                                        1, // has a deadline
                                        0 //  doesn't have a deadline
                                )
                        )
                )
        );
        Bson sortStage = Aggregates.sort(Sorts.orderBy(
                descending("_deadlineOrder"),
                ascending("deadline"),
                ascending("createdDate")
        ));
        Bson projectStage = Aggregates.project(Projections.fields(Projections.excludeId()));
        List<Task> lsts = new ArrayList<>();
        collection.aggregate(asList(addFieldsStage, sortStage, projectStage)).forEach(doc -> lsts.add(Task.fromDocument(doc)));
        return lsts;

    }
}
