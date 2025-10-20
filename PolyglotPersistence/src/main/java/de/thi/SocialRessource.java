package de.thi;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.bson.Document;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

@Path("/api/social")
@Produces(MediaType.APPLICATION_JSON)
public class SocialRessource {

    @Inject SocialService socialService;

    @GET
    public Map<String, Object> getSocialFeed(
            @QueryParam("user") String userId,
            @QueryParam("hours") @DefaultValue("24") int hours
    ) {
        Instant since = Instant.now().minus(hours, ChronoUnit.HOURS);
        List<String> friends = socialService.getFriendsFromNeo4j(userId);
        List<String> onlineFriends = socialService.getOnlineFriendsFromRedis(friends);
        List<Document> posts = socialService.getRecentPostsFromMongo(onlineFriends, since);

        return Map.of(
                "user", userId,
                "friends", friends,
                "onlineFriends", onlineFriends,
                "posts", posts
        );
    }
}
