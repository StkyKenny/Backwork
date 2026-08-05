package me.stky.backwork.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
public class TryingDTO {


    private String title;
    private String content;
    private List<String> tags;

    public TryingDTO() {}

    public TryingDTO(String title, String content, List<String> tags) {
        this.title = title;
        this.content = content;
        this.tags = tags;
    }



}