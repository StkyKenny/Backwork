package me.stky.backwork.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.text.SimpleDateFormat;
import java.util.Random;

public class Helper {
    public static String prettyPrintUsingGson(Object myObject) {
        try {
            ObjectMapper mapper = new ObjectMapper();

            mapper.registerModule(new JavaTimeModule());
            mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
            mapper.setDateFormat(new SimpleDateFormat("yy/MM/dd"));

            return mapper
                    .writerWithDefaultPrettyPrinter()
                    .writeValueAsString(myObject);

        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize object", e);
        }

    }

    public static String rdnName() {
        String alphabet = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
        StringBuilder rdnString = new StringBuilder();
        Random random = new Random();

        for (int i = 0; i < 6 + random.nextInt(10); i++) {
            int index = random.nextInt(alphabet.length());
            rdnString.append(alphabet.charAt(index));
        }
        return rdnString.toString();
    }
}
