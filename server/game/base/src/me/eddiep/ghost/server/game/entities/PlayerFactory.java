package me.eddiep.ghost.server.game.entities;

import me.eddiep.ghost.server.Starter;
import me.eddiep.ghost.server.game.entities.playable.impl.Player;
import me.eddiep.ghost.server.network.dataserv.LoginServerBridge;
import me.eddiep.ghost.server.network.dataserv.PlayerData;
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

    public static Player attemptLogin(String uuid) {
        LoginServerBridge bridge = Starter.getLoginBridge();

        if (bridge.isValidSession(uuid)) {
            PlayerData data = bridge.fetchPlayerStats(uuid);

            Player player = Player.createPlayer(data);

            connectedUsers.put(player.getSession(), player);
            cachedUsernames.put(data.getUsername(), player.getSession());
            cachedIds.put(player.getPlayerID(), player.getSession());

            return player;
        } else {
            return null;
        }
    }

    /*public static Player registerPlayer(String username, PlayerData sqlData) {
        if (findPlayerByUsername(username) != null)
            throw new InvalidParameterException("Username already taken! No check was taken!");

        Player playable = Player.createPlayer(username, sqlData);

        connectedUsers.put(playable.getSession(), playable);
        cachedUsernames.put(username, playable.getSession());
        cachedIds.put(playable.getPlayerID(), playable.getSession());

        return playable;
    }*/
}
