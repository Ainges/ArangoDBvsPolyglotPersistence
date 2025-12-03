package de.thi;
import de.thi.ports.SocialQueryRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.Map;

@ApplicationScoped
public class SocialService {

    @Inject
    SocialQueryRepository socialQueryRepository;

    public Map<String, Object> queryPostsOfOnlineFriends(String userId, int hours) {
        return socialQueryRepository.queryPostsOfOnlineFriends(userId, hours);
    }



}
