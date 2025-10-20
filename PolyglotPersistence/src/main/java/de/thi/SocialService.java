package de.thi;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.bson.Document;
import org.neo4j.driver.*;
import com.mongodb.client.*;
import com.mongodb.client.model.Filters;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@ApplicationScoped
public class SocialService {

    @Inject Driver neo4jDriver;
    @Inject MongoClient mongoClient;
    @Inject
    RedisService redisService;

    public List<String> getFriendsFromNeo4j(String userId) {
        try (Session session = neo4jDriver.session()) {
            Result result = session.run("""
                MATCH (u:User {id:$id})-[:FRIENDS_WITH]->(f:User)
                RETURN f.id AS friendId
            """, Map.of("id", userId));

            List<String> friends = new ArrayList<>();
            while (result.hasNext()) {
                friends.add(result.next().get("friendId").asString());
            }
            return friends;
        }
    }

    public List<String> getOnlineFriendsFromRedis(List<String> friendIds) {
        List<String> online = new ArrayList<>();
        for (String fid : friendIds) {
            try {
                String status = redisService.hgetString("session:" + fid, "online");
                if (status != null && "true".equalsIgnoreCase(status)) {
                    online.add(fid);
                }
            } catch (Exception e) {
                System.err.println("Redis error for key session:" + fid + ": " + e.getMessage());
            }
        }
        return online;
    }

    public List<Document> getRecentPostsFromMongo(List<String> users, Instant since) {
        if (users.isEmpty()) return List.of();
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
        return combined;
    }
}
