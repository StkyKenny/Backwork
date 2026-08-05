package me.stky.backwork.model;

import lombok.Data;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.FieldType;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.util.List;

@Data
@Document(collection = "test")
public class Trying {



    @MongoId(FieldType.OBJECT_ID)
    private String id;
    @Indexed
    private String title;
    private String content;
    private List<String> tags;

    public Trying() {}

    public Trying(String title, String content, List<String> tags) {
        this.title = title;
        this.content = content;
        this.tags = tags;
    }

    public Trying(TryingDTO tryingDTO) {
        this.title = tryingDTO.getTitle();
        this.content = tryingDTO.getContent();
        this.tags = tryingDTO.getTags();
    }
}