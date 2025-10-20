package de.thi;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;


import java.util.Map;

@Path("/api/social")
public class SocialRessource {

    @Inject SocialService socialService;

    @GET
    public Map<String, Object> getSocialFeed(
            @QueryParam("user") String userId,
            @QueryParam("hours") @DefaultValue("24") int hours
    ) {
        return socialService.getSocialFeed(userId, hours);
    }
}
