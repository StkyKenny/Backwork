package me.stky.backwork.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public class Helper {
    public static String prettyPrintUsingGson(Object myObject) {
        try {
            ObjectMapper mapper = new ObjectMapper();

            mapper.registerModule(new JavaTimeModule());
            mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

            return mapper
                    .writerWithDefaultPrettyPrinter()
                    .writeValueAsString(myObject);

        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize object", e);
        }

    }
}
