package com.boxtrotstudio.ghost.gameserver.api.game;

import com.boxtrotstudio.ghost.common.game.NetworkMatch;
import com.boxtrotstudio.ghost.game.queue.Queues;

public interface Game {

    Queues getQueue();

    void onServerStart();

    void onServerStop();

    short getPlayersPerMatch();

    void onMatchPreSetup(NetworkMatch activeMatch);

    void onMatchSetup(NetworkMatch activeMatch);

    String getMapName();
}
