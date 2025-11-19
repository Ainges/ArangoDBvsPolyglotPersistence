package de.thi.adapter;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import de.thi.ports.PostRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.bson.Document;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@ApplicationScoped
public class MongoDbService implements PostRepository {

    @Inject
    MongoClient mongoClient;


    public List<Map<String, Object>> getPostsOfOnlineFriendsSince(List<String> users, Instant since){
        if (users.isEmpty()) return Collections.emptyList();
        MongoDatabase db = mongoClient.getDatabase("admin");
        MongoCollection<Document> postsCol = db.getCollection("posts");
        MongoCollection<Document> interactionsCol = db.getCollection("interactions");

        List<Document> recentPosts = postsCol.find(Filters.and(
                Filters.in("user", users),
                Filters.gte("timestamp", since.toString())
        )).into(new ArrayList<>());

        List<Document> likes = interactionsCol.find(Filters.and(
                Filters.in("user", users),
                Filters.eq("type", "like")
        )).into(new ArrayList<>());

        Set<String> likedIds = likes.stream().map(l -> l.getString("post")).collect(Collectors.toSet());
        List<Document> likedPosts = postsCol.find(Filters.in("id", likedIds)).into(new ArrayList<>());

        List<Document> combined = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        for (Document d : recentPosts) if (seen.add(d.getString("id"))) combined.add(d);
        for (Document d : likedPosts) if (seen.add(d.getString("id"))) combined.add(d);
        return combined.stream().map(d -> {
            Map<String, Object> map = new HashMap<>();
            for (String key : d.keySet()) {
                map.put(key, d.get(key));
            }
            return map;
        }).collect(Collectors.toList());
    };
}
