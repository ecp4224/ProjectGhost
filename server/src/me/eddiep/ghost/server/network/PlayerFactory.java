package me.eddiep.ghost.server.network;

import me.eddiep.ghost.server.game.Player;

import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.UUID;

public class PlayerFactory {
    private static HashMap<UUID, Player> connectedUsers = new HashMap<UUID, Player>();
    private static HashMap<String, UUID> cachedUsernames = new HashMap<>();

    public static Player findPlayerByUUID(UUID uuid) {
        return connectedUsers.get(uuid);
    }

    public static Player findPlayerByUUID(String uuid) {
        return connectedUsers.get(UUID.fromString(uuid));
    }

    public static Player findPlayerByUsername(String username) {
        return connectedUsers.get(cachedUsernames.get(username));
    }

    public static Player registerPlayer(String username) {
        if (findPlayerByUsername(username) != null)
            throw new InvalidParameterException("Username already taken! No check was taken!");

        Player player = Player.createPlayer(username);

        connectedUsers.put(player.getSession(), player);
        cachedUsernames.put(username, player.getSession());

        return player;
    }
}
