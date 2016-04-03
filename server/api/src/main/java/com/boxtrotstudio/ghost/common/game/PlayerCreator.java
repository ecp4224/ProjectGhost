package com.boxtrotstudio.ghost.common.game;

import com.boxtrotstudio.ghost.network.sql.PlayerData;

import java.util.UUID;

public interface PlayerCreator {
    Player findPlayerByUUID(String session);

    Player findPlayerByUUID(UUID uuid);

    Player findPlayerByUsername(String username);

    Player findPlayerById(long id);

    boolean checkSession(String uuid);

    void invalidateSession(String username);

    void invalidateSession(Player p);

    Player registerPlayer(String username, PlayerData sqlData);
}
