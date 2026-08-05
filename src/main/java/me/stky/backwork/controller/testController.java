package me.stky.backwork.controller;


import me.stky.backwork.TryingRepository;
import me.stky.backwork.model.Trying;
import me.stky.backwork.model.TryingDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("test")
public class testController {

    @Autowired
    private TryingRepository tryingRepository;


    private Query createQueryOnId(String title) {
        return new Query(Criteria.where("title").is(title));
    }


    @GetMapping("sike")
    public ResponseEntity<String> checkSikeStatus() {
        return ResponseEntity.ok("OK");
    }


    @GetMapping("/getAll")
    public ResponseEntity<List<Trying>> getAll() {
        return ResponseEntity.ok(tryingRepository.findAll());
    }

    @GetMapping("/get")
    public ResponseEntity<Trying> getByTitle(String title) {
        Optional<Trying> trying = tryingRepository.findByTitle(title);
        return trying.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @PostMapping("/create")
    public ResponseEntity<Trying> create(@RequestBody TryingDTO tryingDTO) {
        Optional<Trying> existing = tryingRepository.findByTitle(tryingDTO.getTitle());
        if (existing.isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(existing.get());
        }
        Trying trying = new Trying(tryingDTO);
        tryingRepository.save(trying);
        return ResponseEntity.status(HttpStatus.CREATED).body(trying);
    }


    @PutMapping("/update")
    public ResponseEntity<Trying> update(@RequestBody TryingDTO tryingDTO) {

        Optional<Trying> existing = tryingRepository.findByTitle(tryingDTO.getTitle());
        if (existing.isEmpty()) {

            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        var trying = existing.get();
        trying.updateFields(tryingDTO);
        tryingRepository.save(trying);
        return ResponseEntity.ok(trying);

    }


    @DeleteMapping("/trying")
    public ResponseEntity<Void> delete(String title) {
        if (!tryingRepository.existsByTitle(title)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

        tryingRepository.deleteByTitle(title);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/trying/count")
    public ResponseEntity<Long> count() {
        long count = tryingRepository.count();
        return ResponseEntity.ok(count);
    }


}
