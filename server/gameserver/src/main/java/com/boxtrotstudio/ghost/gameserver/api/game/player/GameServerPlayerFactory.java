package com.boxtrotstudio.ghost.gameserver.api.game.player;

import com.boxtrotstudio.ghost.common.game.Player;
import com.boxtrotstudio.ghost.common.game.PlayerCreator;
import com.boxtrotstudio.ghost.network.sql.PlayerData;
import com.boxtrotstudio.ghost.utils.Global;

import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.UUID;

public class GameServerPlayerFactory implements PlayerCreator {
    private static final long SESSION_TIMEOUT = 10800000; //3 hours in ms
    public static final GameServerPlayerFactory INSTANCE = new GameServerPlayerFactory();

    private HashMap<String, Player> connectedUsers = new HashMap<>();
    private HashMap<String, String> cachedUsernames = new HashMap<>();
    private HashMap<Long, String> cachedIds = new HashMap<>();

    @Override
    public Player findPlayerByUUID(String session) {
        return connectedUsers.get(session);
    }

    @Override
    public Player findPlayerByUUID(UUID uuid) {
        return connectedUsers.get(uuid.toString());
    }

    @Override
    public Player findPlayerByUsername(String username) {
        return connectedUsers.get(cachedUsernames.get(username));
    }

    @Override
    public Player findPlayerById(long id) {
        return connectedUsers.get(cachedIds.get(id));
    }

    @Override
    public boolean checkSession(String uuid) {
        Player player;
        if ((player = findPlayerByUUID(uuid)) == null)
            return false;

        if (System.currentTimeMillis() - player.getLastActiveTime() >= SESSION_TIMEOUT) {
            invalidateSession(player.getUsername());
            return false;
        }
        return true;
    }

    @Override
    public void invalidateSession(String username) {
        if (findPlayerByUsername(username) == null)
            return;
        Global.LOGGER.info("Ended session for " + username);
        connectedUsers.remove(cachedUsernames.get(username));
    }

    @Override
    public void invalidateSession(Player p) {
        Global.LOGGER.info("Ended session for " + p.getUsername());
        connectedUsers.remove(p.getSession());
    }

    @Override
    public Player registerPlayer(String username, PlayerData sqlData) {
        if (sqlData == null) {
            sqlData = new PlayerData(username, username);
        }

        if (findPlayerByUsername(username) != null)
            throw new InvalidParameterException("Username already taken! No check was taken!");

        UUID session;
        do {
            session = UUID.randomUUID();
        } while (findPlayerByUUID(session) != null);

        Player player = new Player(username, session.toString(), sqlData);

        connectedUsers.put(player.getSession(), player);
        cachedUsernames.put(username, player.getSession());
        cachedIds.put(player.getPlayerID(), player.getSession());

        return player;
    }

    public Player registerPlayer(String username, String session, PlayerData sqlData) {
        if (findPlayerByUsername(username) != null)
            throw new InvalidParameterException("Username already taken! No check was taken!");

        Player player = new Player(username, session, sqlData);

        connectedUsers.put(player.getSession(), player);
        cachedUsernames.put(username, player.getSession());
        cachedIds.put(player.getPlayerID(), player.getSession());

        return player;
    }
}
