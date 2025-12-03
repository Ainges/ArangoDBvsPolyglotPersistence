package de.thi;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.DefaultValue;

import java.util.Map;

@Path("/social")
@Produces(MediaType.APPLICATION_JSON)
public class SocialMultiModalRessource {

    @Inject
    SocialService socialService;

    @GET
    @Path("/feed")
    public Map<String, Object> getSocialFeed(
            @QueryParam("user") @DefaultValue("alice") String userId,
            @QueryParam("hours") @DefaultValue("17520") int hours // = 24*365*2 -> 2 years
    ) {
        Map<String, Object> response = socialService.queryPostsOfOnlineFriends(userId, hours);
        return response;
    }
}
