package de.thi;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;


import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

@Path("/api/social")
@Produces(MediaType.APPLICATION_JSON)
public class SocialRessource {

    @Inject
    SocialService socialService;

    @GET
    public Map<String, Object> getSocialFeed(
            @QueryParam("user") @DefaultValue("alice") String userId,
            @QueryParam("hours") @DefaultValue("17520") int hours // = 24*365*2 -> 2 years
    ) {
        Instant since = Instant.now().minus(hours, ChronoUnit.HOURS);
        List<String> friends = socialService.getFriendsFromNeo4j(userId);
        List<String> onlineFriends = socialService.getOnlineFriendsOfUser(friends);
        List<Map<String, Object>> posts = socialService.getPostsOfOnlineFriendsSince(onlineFriends, since);

        return Map.of(
                "friends", friends,
                "posts", posts,
                "user", userId,
                "onlineFriends", onlineFriends

        );
    }
}
