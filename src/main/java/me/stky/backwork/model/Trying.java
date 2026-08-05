package me.stky.backwork.model;

import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.FieldType;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.time.Instant;
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

    @CreatedDate
    private Instant createdDate;
    @LastModifiedDate
    private Instant updatedDate;


    public Trying() {}

    public Trying(String title, String content, List<String> tags) {
        this.title = title;
        this.content = content;
        this.tags = tags;
    }

    private void copyFromDTO(TryingDTO dto) {
        this.title = dto.getTitle();
        this.content = dto.getContent();
        this.tags = dto.getTags();
    }

    public Trying(TryingDTO tryingDTO) {
        copyFromDTO(tryingDTO);
    }

    public void updateFields(TryingDTO tryingDTO) {
        copyFromDTO(tryingDTO);
    }
}