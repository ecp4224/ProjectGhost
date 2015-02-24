package me.eddiep.ghost.server.network;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class UserFactory {
    private static HashMap<UUID, User> connectedUsers = new HashMap<UUID, User>();
    private static HashMap<String, UUID> cachedUsernames = new HashMap<>();

    public static User findUserByUUID(UUID uuid) {
        return connectedUsers.get(uuid);
    }

    public static User findUserByUUID(String uuid) {
        return connectedUsers.get(UUID.fromString(uuid));
    }

    public static User findUserByUsername(String username) {
        return connectedUsers.get(cachedUsernames.get(username));
    }

    public static User registerUser(String username) {
        if (findUserByUsername(username) != null)
            throw new InvalidParameterException("Username already taken! No check was taken!");

        User user = User.createUser(username);

        connectedUsers.put(user.getSession(), user);
        cachedUsernames.put(username, user.getSession());

        return user;
    }
}
