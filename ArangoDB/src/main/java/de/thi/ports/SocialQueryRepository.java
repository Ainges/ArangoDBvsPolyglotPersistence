package de.thi.ports;

import java.util.Map;

public interface SocialQueryRepository {

     Map<String, Object> queryPostsOfOnlineFriends(String userId, int hours);
}
