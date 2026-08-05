package me.stky.backwork;

import com.mongodb.client.MongoCollection;
import me.stky.backwork.model.Trying;
import org.bson.Document;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.DeleteQuery;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TryingRepository extends MongoRepository<Trying, String> {


    List<Trying> findByTitle(String title);

    @Query("{ 'tags': { $in: [?1] } }")
    List<Trying> findByTag(String tag);

    @Query("{ 'content': new RegExp(?, '$i') }")
    List<Trying> findByContentRegex(String pattern);

    long count();

}
