package me.eddiep.ghost.matchmaking.player;

import java.util.UUID;

public interface PlayerFactory {
    Player findPlayerBySession(UUID uuid);

    Player findPlayerBySession(String uuid);

    Player findPlayerByUsername(String username);

    Player findPlayerById(long id);


}
