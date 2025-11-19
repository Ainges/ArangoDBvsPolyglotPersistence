package de.thi.ports;

import java.util.List;

public interface FriendsRepository {
    List<String> getFriendsOfUser(String userId);
}
