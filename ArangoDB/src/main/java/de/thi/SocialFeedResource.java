package de.thi;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.DefaultValue;
import com.arangodb.ArangoCursor;
import com.arangodb.ArangoDB;
import com.arangodb.ArangoDatabase;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;

@Path("/social")
@Produces(MediaType.APPLICATION_JSON)
public class SocialFeedResource {

    @Inject
    ArangoDB arangoDB;

    @GET
    @Path("/feed")
    public Map<String, Object> getSocialFeed(
            @QueryParam("user") @DefaultValue("alice") String userId,
            @QueryParam("hours") @DefaultValue("36000") int hours
    ) {
        // Datenbank-Instanz holen
        ArangoDatabase db = arangoDB.db("_system");

        // AQL Query definieren
        String query = """
            LET hoursAgo = DATE_SUBTRACT(DATE_NOW(), @hours, "hours")
            LET friends = (
                FOR edge IN friendships
                    FILTER edge._from == CONCAT('users/', @userId)
                    RETURN PARSE_IDENTIFIER(edge._to).key
            )
            LET onlineFriends = (
                        FOR session IN sessions
                            FILTER session.online == true
                            FILTER session.user IN friends
                            FOR u IN users
                                FILTER u.id == session.user
                                RETURN MERGE(u, session)
                    )
        
            LET posts = (
                FOR p IN posts
                    FILTER p.user IN onlineFriends[*].id
                    FILTER p.timestamp >= hoursAgo
                    RETURN p
            )
            LET likes = (
                FOR inter IN interactions
                    FILTER inter.type == "like"
                    LET friend = SPLIT(inter._from, "/")[1]
                    FILTER friend IN onlineFriends[*].id
                    LET postId = SPLIT(inter._to, "/")[1]
                    FOR p IN posts
                        FILTER p.id == postId
                        FILTER p.timestamp >= hoursAgo
                        RETURN {
                            user: friend,
                            post: p
                        }
            )
            LET activeUserIds = UNIQUE(
                UNION(
                    posts[*].user,
                    likes[*].user
                )
            )
            RETURN {
                friends: friends,
                user: @userId,
                posts: posts,
                onlineFriends: onlineFriends
            }
        """;

        // Bind-Parameter definieren
        Map<String, Object> bindVars = new HashMap<>();
        bindVars.put("userId", userId);
        bindVars.put("hours", hours);

        // Query ausführen
        ArangoCursor<Map> cursor = db.query(
                query,
                Map.class,           // Type zuerst
                bindVars,            // Dann bindVars
                null                 // Dann options (kann null sein)
        );


        // Ergebnis extrahieren
        if (cursor.hasNext()) {
            return (Map<String, Object>) cursor.next();
        }

        // Fallback, falls keine Daten gefunden wurden
        return Map.of(
                "friends", new ArrayList<>(),
                "posts", new ArrayList<>(),
                "user", userId,
                "onlineFriends", new ArrayList<>()
        );
    }
}
