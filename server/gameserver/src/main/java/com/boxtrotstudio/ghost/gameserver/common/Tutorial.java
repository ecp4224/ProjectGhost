package com.boxtrotstudio.ghost.gameserver.common;

import com.boxtrotstudio.ghost.common.game.NetworkMatch;
import com.boxtrotstudio.ghost.game.queue.Queues;
import com.boxtrotstudio.ghost.gameserver.api.game.Game;

public class Tutorial implements Game {
    @Override
    public Queues getQueue() {
        return Queues.TUTORIAL;
    }

    @Override
    public void onServerStart() { }

    @Override
    public void onServerStop() { }

    @Override
    public short getPlayersPerMatch() {
        return 1;
    }

    @Override
    public void onMatchPreSetup(NetworkMatch activeMatch) {

    }

    @Override
    public void onMatchSetup(NetworkMatch activeMatch) {

    }

    @Override
    public String getMapName() {
        return "tutorial";
    }
}
