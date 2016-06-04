package com.boxtrotstudio.ghost.gameserver.common;

import com.boxtrotstudio.ghost.game.match.abilities.Gun;
import com.boxtrotstudio.ghost.game.match.entities.PlayableEntity;
import com.boxtrotstudio.ghost.gameserver.api.game.Game;
import com.boxtrotstudio.ghost.common.game.NetworkMatch;
import com.boxtrotstudio.ghost.game.queue.Queues;

public class TestGame implements Game {
    @Override
    public Queues getQueue() {
        return Queues.ORIGINAL;
    }

    @Override
    public void onServerStart() {
        System.out.println("Server Started!");
    }

    @Override
    public void onServerStop() {
        System.out.println("Server Stopped!");
    }

    @Override
    public short getPlayersPerMatch() {
        return 2;
    }

    @Override
    public void onMatchPreSetup(NetworkMatch activeMatch) {

    }

    @Override
    public void onMatchSetup(NetworkMatch activeMatch) {
        for (PlayableEntity p : activeMatch.getPlayers()) {
            p.setLives((byte) 3);
            p.setCurrentAbility(Gun.class);
        }
    }

    @Override
    public String getMapName() {
        return "test";
    }
}
