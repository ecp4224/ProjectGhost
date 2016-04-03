package com.boxtrotstudio.ghost.matchmaking.player;

import com.boxtrotstudio.ghost.matchmaking.network.gameserver.Stream;
import com.boxtrotstudio.ghost.network.sql.PlayerData;

import java.io.IOException;
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
        Player p;
        if ((p = findPlayerByUsername(username)) == null)
            return;
        System.out.println("[SERVER] Ended session for " + username);
        invalidateSession(p);
    }

    public static void invalidateSession(Player p) {
        System.out.println("[SERVER] Ended session for " + p.getUsername());
        connectedUsers.remove(p.getSession());
        cachedUsernames.remove(p.getUsername());
        cachedIds.remove(p.getPlayerID());
    }


    public static Player registerPlayer(String username, PlayerData sqlData, Stream streamToJoin) {
        Player currentlyLogged;
        if ((currentlyLogged = findPlayerByUsername(username)) != null) {
            try {
                currentlyLogged.getClient().disconnect(); //Logout currently logged in player
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        UUID session;
        do {
            session = UUID.randomUUID();
        } while (connectedUsers.containsKey(session.toString()));

        Player player = new Player(session.toString(), sqlData, streamToJoin);

        connectedUsers.put(player.getSession(), player);
        cachedUsernames.put(username, player.getSession());
        cachedIds.put(player.getPlayerID(), player.getSession());

        return player;
    }

    public static boolean checkSession(String session) {
        return connectedUsers.containsKey(session);
    }
}
