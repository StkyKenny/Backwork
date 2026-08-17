package me.stky.backwork.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.bson.Document;

@Data
@AllArgsConstructor
public class Task {


    private String name;
    private String description;


    public Task() {

    }

    public Document toDocument() {
        return new Document().append("name", name)
                .append("description", description);
    }

    public static Task fromDocument(Document document) {
        // construct Task from document
        var task = new Task();
        task.setName(document.getString("name"));
        task.setDescription(document.getString("description"));

        return task;
    }

}
