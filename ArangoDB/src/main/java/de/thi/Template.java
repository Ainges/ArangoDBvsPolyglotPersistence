package de.thi;

import jakarta.inject.Inject;
import org.eclipse.jnosql.databases.arangodb.mapping.ArangoDBTemplate;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Template {

    @Inject
    ArangoDBTemplate template;

    Map<String, Object> params = Map.of("userId", "alice", "hours", 24);

    String aql = """
LET hoursAgo = DATE_SUBTRACT(DATE_NOW(), @hours, "hours")
LET friends = (
    FOR edge IN friendships
        FILTER edge._from == CONCAT('users/', @userId)
        RETURN PARSE_IDENTIFIER(edge._to).key
)
LET onlineFriends = (
    FOR status IN user_status
        FILTER status.user IN friends
        FILTER status.online == true
        FOR u IN users
            FILTER u.id == status.user
            RETURN MERGE(u, status)
)
LET posts = (
    FOR p IN posts
        FILTER p.user IN onlineFriends[*].id
        FILTER p.timestamp >= hoursAgo
        RETURN p
)
LET likes = (
    FOR inter IN interactions
        FILTER inter.type=="like"
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
LET activeFriendsOnline = (
    FOR f IN onlineFriends
        FILTER f.id IN activeUserIds
        RETURN f
)
RETURN {
    user: @userId,
    friends: friends,
    onlineFriends: onlineFriends,
    activeFriendsOnline: activeFriendsOnline,
    posts: posts
}
""";

    public String runQuery() {
        // Führt die AQL-Abfrage mit Parametern aus
        List<Map<String, Object>> result = template.query(aql, params);

        // Wandelt das Ergebnis in einen lesbaren String um
        return result.stream()
                .map(Object::toString)
                .collect(Collectors.joining("\n"));
    }



}
