package de.thi;

import de.thi.ports.FriendsRepository;
import de.thi.ports.PostRepository;
import de.thi.ports.SessionRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.Instant;
import java.util.*;

@ApplicationScoped
public class SocialService {

    // Neo4J
    @Inject
    FriendsRepository friendsRepository;
    // Mongo
    @Inject
    PostRepository postRepository;
    // REDIS
    @Inject
    SessionRepository sessionRepository;

    public List<String> getFriendsFromNeo4j(String userId) {
        return friendsRepository.getFriendsOfUser(userId);
    }

    public List<String> getOnlineFriendsOfUser(List<String> friendIds) {
        return sessionRepository.getOnlineFriendsOfUser(friendIds);
    }

    public List<Map<String, Object>> getPostsOfOnlineFriendsSince(List<String> users, Instant since) {
        return postRepository.getPostsOfOnlineFriendsSince(users, since);
    }
}
