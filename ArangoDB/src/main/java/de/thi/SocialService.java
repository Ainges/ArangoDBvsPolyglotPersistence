package de.thi;

import java.util.Map;
import java.util.stream.Collectors;

public class SocialService {
    public Map<String, Object> getSocialFeed(String userId, int hours) {
        String aql = "..."; // Siehe oben
        Map<String, Object> params = Map.of("userId", userId, "hours", hours);
        var result = template.aql(aql, params).collect(Collectors.toList());
        // Optional: Mapping zu eigenen Result-Strukturen oder direkte Rückgabe
        return result.isEmpty() ? Map.of("error", "no data found") : (Map<String,Object>)result.get(0);
    }

}
