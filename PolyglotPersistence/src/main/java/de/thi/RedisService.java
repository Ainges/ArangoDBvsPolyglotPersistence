package de.thi;

import io.quarkus.redis.datasource.RedisDataSource;
import io.quarkus.redis.datasource.hash.HashCommands;
import io.quarkus.redis.datasource.value.ValueCommands;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class RedisService {

    private final ValueCommands<String, Long> countCommands;
    private final HashCommands<String, String, Long> hashCommandsLong;
    private final HashCommands<String, String, String> hashCommandsString;

    public RedisService(RedisDataSource ds) {
        this.countCommands = ds.value(Long.class);
        this.hashCommandsLong = ds.hash(Long.class);
        this.hashCommandsString = ds.hash(String.class);
    }

    long get(String key) {
        Long value = countCommands.get(key);
        return value == null ? 0L : value;
    }

    public long hget(String key, String field) {
        Long value = hashCommandsLong.hget(key, field);
        return value == null ? 0L : value;
    }

    public String hgetString(String key, String field) {
        return hashCommandsString.hget(key, field);
    }
}
