package de.thi.ports;

import java.util.List;

public interface SessionRepository {
    List<String> getOnlineFriendsOfUser(List<String> friendIds);
}
