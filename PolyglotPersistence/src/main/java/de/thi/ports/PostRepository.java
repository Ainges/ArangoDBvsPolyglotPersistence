package de.thi.ports;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public interface PostRepository {

    List<Map<String, Object>> getPostsOfOnlineFriendsSince(List<String> users, Instant since);

}
