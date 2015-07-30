package me.eddiep.ghost.matchmaking.player;

import me.eddiep.ghost.network.sql.PlayerData;

import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.UUID;

public class PlayerFactory {
    private static final long SESSION_TIMEOUT = 10800000; //3 hours in ms
    private static HashMap<String, Player> connectedUsers = new HashMap<String, Player>();
    private static HashMap<String, String> cachedUsernames = new HashMap<>();
    private static HashMap<Long, String> cachedIds = new HashMap<>();

    public static Player findPlayerByUUID(UUID uuid) {
        return connectedUsers.get(uuid.toString());
    }

    public static Player findPlayerByUUID(String uuid) {
        return connectedUsers.get(uuid);
    }

    public static Player findPlayerByUsername(String username) {
        return connectedUsers.get(cachedUsernames.get(username));
    }

    public static Player findPlayerById(long id) {
        return connectedUsers.get(cachedIds.get(id));
    }

    public static void invalidateSession(String username) {
        if (findPlayerByUsername(username) == null)
            return;
        System.out.println("[SERVER] Ended session for " + username);
        connectedUsers.remove(cachedUsernames.get(username));
    }

    public static void invalidateSession(Player p) {
        System.out.println("[SERVER] Ended session for " + p.getUsername());
        connectedUsers.remove(p.getSession());
    }


    public static Player registerPlayer(String username, PlayerData sqlData) {
        if (findPlayerByUsername(username) != null)
            throw new InvalidParameterException("Username already taken! No check was taken!");

        UUID session;
        do {
            session = UUID.randomUUID();
        } while (connectedUsers.containsKey(session.toString()));

        Player player = new Player(session.toString(), sqlData);

        connectedUsers.put(player.getSession(), player);
        cachedUsernames.put(username, player.getSession());
        cachedIds.put(player.getPlayerID(), player.getSession());

        return player;
    }
}
