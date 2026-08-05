package me.stky.backwork.controller;


import com.mongodb.client.MongoClient;
import me.stky.backwork.TryingRepository;
import me.stky.backwork.model.Trying;
import me.stky.backwork.model.TryingDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("test")
public class testController {

    @Autowired
    private TryingRepository tryingRepository;

    @GetMapping("sike")
    public ResponseEntity<String> checkSikeStatus() {
        return ResponseEntity.ok("OK");
    }


    @GetMapping("/trying")
    public ResponseEntity<List<Trying>> getAll() {
        return ResponseEntity.ok(tryingRepository.findAll());
    }



    @PostMapping("/trying")
    public ResponseEntity<Trying> create(@RequestBody TryingDTO tryingDTO) {
        Trying trying = new Trying(tryingDTO);
        tryingRepository.save(trying);
        return ResponseEntity.status(HttpStatus.CREATED).body(trying);
    }

    /*@PutMapping("/trying/{id}")
    public ResponseEntity<Trying> update(@PathVariable String id,
                                         @RequestBody Trying trying) {
        if (!tryingRepository.existsById(id)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

        tryingRepository.save(trying);
        return ResponseEntity.ok(tryingRepository.findById(id));
    }*/


    @DeleteMapping("/trying/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        if (!tryingRepository.existsById(id)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

        tryingRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/trying/count")
    public ResponseEntity<Long> count() {
        long count = tryingRepository.count();
        return ResponseEntity.ok(count);
    }



}
