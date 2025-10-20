package de.thi;

import io.quarkus.redis.datasource.RedisDataSource;
import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.annotations.QuarkusMain;

import io.vertx.mutiny.redis.client.RedisAPI;
import io.vertx.mutiny.redis.client.Response;
import jakarta.inject.Inject;

import org.neo4j.driver.Driver;
import org.neo4j.driver.Session;
import org.neo4j.driver.Result;
import org.neo4j.driver.Record;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;

import org.bson.Document;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;


@QuarkusMain
public class PolyglotMain implements QuarkusApplication {

    // --- Neo4j (Graph-Daten) ---
    @Inject
    Driver neo4jDriver;

    // --- MongoDB (Posts, Interaktionen, User) ---
    @Inject
    MongoClient mongoClient;

    // --- Redis (Session-Status) ---
    @Inject
    RedisAPI redisAPI; // Low-Level Client


    @Override
    public int run(String... args) throws Exception {

        System.out.println("=== Polyglot Social Network Query ===");
        String userId = "alice";
        Instant since = Instant.now().minus(24, ChronoUnit.HOURS);

        // 1️⃣ Schritt: Freunde aus Neo4j abrufen
        List<String> friendIds = getFriendsFromNeo4j(userId);
        System.out.println("Neo4j → Freunde von " + userId + ": " + friendIds);

        // 2️⃣ Schritt: Online-Freunde aus Redis ermitteln
        List<String> onlineFriends = filterOnlineFriends(friendIds);
        System.out.println("Redis → Online-Freunde: " + onlineFriends);

        // 3️⃣ Schritt: Aktuelle Posts oder Likes dieser Online-Freunde aus MongoDB abrufen
        List<Document> posts = getRecentPostsFromMongo(onlineFriends, since);
        System.out.println("MongoDB → Gefundene Posts/Interaktionen der letzten 24h:");
        posts.forEach(p -> System.out.println(" - " + p.toJson()));

        System.out.println("\n✅ Abfrage abgeschlossen.");
        Quarkus.waitForExit();
        return 0;
    }

    // --- Neo4j: Freunde eines Users holen ---
    private List<String> getFriendsFromNeo4j(String userId) {
        try (Session session = neo4jDriver.session()) {
            Result result = session.run("""
                MATCH (u:User {id:$id})-[:FRIENDS_WITH]->(f:User)
                RETURN f.id AS friendId
            """, Map.of("id", userId));

            List<String> friends = new ArrayList<>();
            while (result.hasNext()) {
                Record rec = result.next();
                friends.add(rec.get("friendId").asString());
            }
            return friends;
        }
    }

    public List<String> filterOnlineFriends(List<String> friendIds) {
        List<String> online = new ArrayList<>();
        if (friendIds == null || friendIds.isEmpty()) return online;

        for (String fid : friendIds) {
            String key = "session:" + fid + ":online";

            try {
                Response resp = redisAPI.get(key).await().indefinitely(); // Low-Level GET
                if (resp != null && "true".equalsIgnoreCase(resp.toString())) {
                    online.add(fid);
                }
            } catch (Exception e) {
                System.err.println("Redis error for key " + key + ": " + e.getMessage());
            }
        }

        return online;
    }



    // --- MongoDB: Posts oder Likes der letzten 24h finden ---
    private List<Document> getRecentPostsFromMongo(List<String> users, Instant since) {
        if (users.isEmpty()) return List.of();

        MongoDatabase db = mongoClient.getDatabase("social");
        MongoCollection<Document> postsCol = db.getCollection("posts");
        MongoCollection<Document> interactionsCol = db.getCollection("interactions");

        // Posts der letzten 24h
        List<Document> recentPosts = postsCol
                .find(Filters.and(
                        Filters.in("user", users),
                        Filters.gte("timestamp", since.toString())
                ))
                .into(new ArrayList<>());

        // Likes dieser User
        List<Document> liked = interactionsCol
                .find(Filters.and(
                        Filters.in("user", users),
                        Filters.eq("type", "like")
                ))
                .into(new ArrayList<>());

        // Alle gelikten Post-IDs sammeln
        Set<String> likedPostIds = liked.stream()
                .map(l -> l.getString("post"))
                .collect(Collectors.toSet());

        // Gelikte Posts laden
        List<Document> likedPosts = postsCol
                .find(Filters.in("id", likedPostIds))
                .into(new ArrayList<>());

        // Kombinieren und Deduplizieren
        Set<String> seen = new HashSet<>();
        List<Document> all = new ArrayList<>();
        for (Document d : recentPosts) {
            if (seen.add(d.getString("id"))) all.add(d);
        }
        for (Document d : likedPosts) {
            if (seen.add(d.getString("id"))) all.add(d);
        }
        return all;
    }

    public static void main(String... args) {
        Quarkus.run(PolyglotMain.class, args);
    }
}
