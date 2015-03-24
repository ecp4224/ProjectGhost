package me.eddiep.ghost.server.game.entities;

import me.eddiep.ghost.server.network.sql.PlayerData;

import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.UUID;

public class PlayerFactory {
    private static final long SESSION_TIMEOUT = 10800000; //3 hours in ms
    private static HashMap<UUID, Player> connectedUsers = new HashMap<UUID, Player>();
    private static HashMap<String, UUID> cachedUsernames = new HashMap<>();
    private static HashMap<Long, UUID> cachedIds = new HashMap<>();

    public static Player findPlayerByUUID(UUID uuid) {
        return connectedUsers.get(uuid);
    }

    public static Player findPlayerByUUID(String uuid) {
        return connectedUsers.get(UUID.fromString(uuid));
    }

    public static Player findPlayerByUsername(String username) {
        return connectedUsers.get(cachedUsernames.get(username));
    }

    public static Player findPlayerById(long id) {
        return connectedUsers.get(cachedIds.get(id));
    }

    public static boolean checkSession(String uuid) {
        Player player;
        if ((player = findPlayerByUUID(uuid)) == null)
            return false;

        if (System.currentTimeMillis() - player.getLastActiveTime() >= SESSION_TIMEOUT) {
            invalidateSession(player.getUsername());
            return false;
        }
        return true;
    }

    public static void invalidateSession(String username) {
        if (findPlayerByUsername(username) == null)
            return;
        System.out.println("[SERVER] Ended session for " + username);
        connectedUsers.remove(cachedUsernames.get(username));
    }

    public static Player registerPlayer(String username, PlayerData sqlData) {
        if (findPlayerByUsername(username) != null)
            throw new InvalidParameterException("Username already taken! No check was taken!");

        Player player = Player.createPlayer(username, sqlData);

        connectedUsers.put(player.getSession(), player);
        cachedUsernames.put(username, player.getSession());
        cachedIds.put(player.getPlayerID(), player.getSession());

        return player;
    }
}
