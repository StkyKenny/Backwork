package me.stky.backwork.controller;


import com.mongodb.client.MongoClient;
import me.stky.backwork.TryingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("mongodebug")
public class MongoDBDebugController {

    @Autowired
    private TryingRepository tryingRepository;

    private String testCollectionName = "test";

    @GetMapping("sike")
    public ResponseEntity<String> checkSikeStatus() {
        return ResponseEntity.ok("OK");
    }


    @GetMapping("hello") public ResponseEntity<?> getAll()
    {
        return ResponseEntity.ok(
                this.tryingRepository.findAll());
    }

    @Autowired
    private MongoTemplate mongoTemplate;

    @GetMapping("dbinfo")
    public String dbInfo() {
        return mongoTemplate.getDb().runCommand(new org.bson.Document("buildInfo", 1)).toJson();
    }

    @GetMapping("count")
    public long count() {
        return mongoTemplate.getCollection(testCollectionName).countDocuments();
    }


    @Autowired
    private MongoClient mongoClient;

    @GetMapping("server")
    public String server() {
        return mongoClient.listDatabaseNames().into(new ArrayList<>()).toString();
    }


    @GetMapping("/debug")
    public String debug() {

        System.out.println("DB = " + mongoTemplate.getDb().getName());

        mongoTemplate.getCollection(testCollectionName)
                .find()
                .forEach(doc -> System.out.println(doc.toJson()));

        return "done | check log";
    }

    @GetMapping("/collections")
    public List<String> collections() {
        return mongoTemplate.getCollectionNames().stream().toList();
    }

    @GetMapping("/factory")
    public String factory() {
        return mongoTemplate.getMongoDatabaseFactory().getMongoDatabase().getName();
    }
}
