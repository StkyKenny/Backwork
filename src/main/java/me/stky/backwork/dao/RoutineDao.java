package me.stky.backwork.dao;

import com.mongodb.client.*;
import com.mongodb.client.model.*;
import com.mongodb.client.result.UpdateResult;
import lombok.extern.slf4j.Slf4j;
import me.stky.backwork.model.routine.Routine;
import me.stky.backwork.service.MongoService;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.mongodb.client.model.Sorts.ascending;
import static java.util.Arrays.asList;

@Slf4j
@Repository
public class RoutineDao {

    private final MongoClient mongoClient;
    private final MongoDatabase database;
    private final MongoCollection<Document> collection;
    private final MongoCollection<Document> collection_archive;

    public RoutineDao(MongoService mongoService) {
        this.database = mongoService.getDatabase();
        this.mongoClient = mongoService.getMongoClient();
        this.collection = database.getCollection("routines");
        this.collection_archive = database.getCollection("routines_archive");
    }

    private void createIndexes() {
        // Indexing routines based on next scheduled date and status
        collection.createIndex(
                Indexes.compoundIndex(
                        Indexes.ascending("nextScheduledDate"),
                        Indexes.ascending("status")
                )
        );
    }

    /**
     * Inserts a new Routine into the database.
     *
     * @param routine The routine to add.
     * @return The inserted ObjectId.
     * @throws SQLException if insertion fails.
     */
    public Routine addRoutine(Routine routine) throws SQLException {
        // Check if routine already exists (using a combination of name and path for uniqueness check)
        var find = collection.find(getRoutineIDFilter(routine)).first();
        if (find != null) {
            throw new IllegalArgumentException("The Routine " + routine.getName() + " already exists in DB");
        }
        var result = collection.insertOne(routine.toDocument());
        if (!result.wasAcknowledged()) {
            throw new SQLException("Failed to insert routine");
        }
        ObjectId insertedId = result.getInsertedId().asObjectId().getValue();

        // 3. Fetch the complete document using the generated ID
        Document insertedDocument = collection.find(getRoutineIDFilter(insertedId)).first();


        if (insertedDocument == null) {
            // This should theoretically never happen if insertOne succeeded
            throw new SQLException("Insertion succeeded but failed to retrieve the document.");
        }

        // 4. Convert the document back to a Routine object
        return Routine.fromDocument(insertedDocument);
    }

    /**
     * Updates an existing Routine in the database.
     *
     * @param routine        The routine object containing the ID to update.
     * @param updatedRoutine The routine object with new data.
     * @return The ObjectId of the updated routine.
     * @throws SQLException if update fails.
     */
    public Routine replaceRoutine(Routine routine, Routine updatedRoutine) throws SQLException {
        var result = collection.findOneAndReplace(getRoutineIDFilter(routine), updatedRoutine.toDocument());
        if (result != null) {
            return getRoutineFromId(result.getObjectId("_id"));
        } else {
            throw new SQLException("Failed to replace routine");
        }
    }

    /**
     * Retrieves all routines sorted by the nearest upcoming deadline/next scheduled date.
     *
     * @return List of routines.
     */
    public List<Routine> getRoutineSortedByNextDate() {
        // Aggregation pipeline to sort by nextScheduledDate
        List<Bson> pipeline = asList(
                Aggregates.sort(Sorts.orderBy(ascending("nextScheduledDate"))),
                Aggregates.project(Projections.fields(Projections.excludeId()))
        );

        List<Routine> routines = new ArrayList<>();
        collection.aggregate(pipeline).forEach(doc -> routines.add(Routine.fromDocument(doc)));
        return routines;
    }

    /**
     * Deletes the Routine from the DB.
     *
     * @param routine The Routine to delete.
     * @return The deleted Routine.
     * @throws SQLException if the Routine wasn't deleted.
     */
    public Routine deleteRoutine(Routine routine) throws SQLException {
        var result = collection.deleteOne(getRoutineIDFilter(routine));
        if (result.wasAcknowledged() && result.getDeletedCount() == 1) {
            return routine;
        } else {
            throw new SQLException("Failed to remove routine");
        }
    }

    /**
     * Sets the Routine status to Completed.
     *
     * @param routine The Routine to complete.
     * @return The updated Routine from the Database.
     * @throws SQLException if the transaction wasn't acknowledged.
     */
    public Routine completeRoutine(Routine routine) throws SQLException {
        UpdateResult result;
        replaceRoutine(routine, routine);
        result = collection.updateOne(
                getRoutineIDFilter(routine),
                Updates.combine(
                        Updates.set("status", "COMPLETED"),
                        Updates.set(Routine.KEY_LAST_EXECUTED, Instant.now())
                ));


        if (!result.wasAcknowledged()) {
            throw new SQLException("Failed to complete routine");
        }
        return collection.find(getRoutineIDFilter(routine)).map(Routine::fromDocument).first();
    }


    /**
     * Archives the routine by moving it to the archive collection and setting it to status ARCHIVED.
     * Uses a transaction for atomicity.
     *
     * @param routine The Routine to archive.
     * @throws SQLException if the transaction fails.
     */
    public void archiveRoutine(Routine routine) throws SQLException {

        final ClientSession clientSession = mongoClient.startSession();

        TransactionBody<String> txnBody = new TransactionBody<>() {
            public String execute() {
                // Find and delete from main collection
                var updatedRoutineDocument = collection.findOneAndDelete(getRoutineIDFilter(routine));
                if (updatedRoutineDocument == null) {
                    throw new RuntimeException("Couldn't find and delete the routine to archive");
                }

                // Update status and insert into archive collection
                Routine updatedRoutine = Routine.fromDocument(updatedRoutineDocument);
                collection_archive.insertOne(updatedRoutine.toDocument());

                return "Archived :" + routine.getName();
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
     * Creates the filter used for finding the correct Routine.
     * Defined by the name and the target executable path.
     *
     * @param routine The routine to search.
     * @return The MongoDB Filter.
     */
    private Bson getRoutineIDFilter(Routine routine) {
        return Filters.eq("_id", routine.get_id());
    }

    private Bson getRoutineIDFilter(ObjectId routineId) {
        return Filters.eq("_id", routineId);
    }


    private Routine getRoutineFromId(ObjectId routineId) {
        return Routine.fromDocument(Objects.requireNonNull(collection.find(getRoutineIDFilter(routineId)).first()));
    }
}