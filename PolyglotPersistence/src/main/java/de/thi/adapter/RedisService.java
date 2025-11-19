package de.thi.adapter;

import de.thi.ports.SessionRepository;
import io.quarkus.redis.datasource.RedisDataSource;
import io.quarkus.redis.datasource.hash.HashCommands;
import io.quarkus.redis.datasource.value.ValueCommands;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class RedisService implements SessionRepository {

    private final ValueCommands<String, Long> countCommands;
    private final HashCommands<String, String, Long> hashCommandsLong;
    private final HashCommands<String, String, String> hashCommandsString;

    public RedisService(RedisDataSource ds) {
        this.countCommands = ds.value(Long.class);
        this.hashCommandsLong = ds.hash(Long.class);
        this.hashCommandsString = ds.hash(String.class);
    }

    private long get(String key) {
        Long value = countCommands.get(key);
        return value == null ? 0L : value;
    }

    private long hget(String key, String field) {
        Long value = hashCommandsLong.hget(key, field);
        return value == null ? 0L : value;
    }

    public String hgetString(String key, String field) {
        return hashCommandsString.hget(key, field);
    }

    @Override
    public List<String> getOnlineFriendsOfUser(List<String> friendIds) {
        List<String> online = new ArrayList<>();
        for (String fid : friendIds) {
            try {
                String status = this.hgetString("session:" + fid, "online");
                if (status != null && "true".equalsIgnoreCase(status)) {
                    online.add(fid);
                }
            } catch (Exception e) {
                System.err.println("Redis error for key session:" + fid + ": " + e.getMessage());
            }
        }
        return online;
    }
}
