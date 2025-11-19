package de.thi.adapter;

import de.thi.ports.FriendsRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class Neo4JService implements FriendsRepository {

    @Inject
    Driver neo4jDriver;


    @Override
    public List<String> getFriendsOfUser(String userId) {
        List<String> friends = new ArrayList<>();
        try (Session session = neo4jDriver.session()) {
            Result result = session.run("""
                MATCH (u:User {id:$id})-[:FRIENDS_WITH]->(f:User)
                RETURN f.id AS friendId
            """, Map.of("id", userId));

            while (result.hasNext()) {
                friends.add(result.next().get("friendId").asString());
            }
        }
        return friends;
    }
}
