package me.stky.backwork.service;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import lombok.Getter;
import org.springframework.stereotype.Service;

@Service
public class MongoService {


    private final String connectionString = "mongodb://localhost:27017/";
    @Getter
    private final MongoClient mongoClient;
    @Getter
    private final MongoDatabase database;


    public MongoService() {
        // MongoClient is thread-safe and should be reused across the application
        mongoClient = MongoClients.create(connectionString);
        this.database = mongoClient.getDatabase("backwork_db");
    }

}
