package com.boxtrotstudio.ghost.gameserver.common;

import com.boxtrotstudio.ghost.gameserver.api.game.Game;
import com.boxtrotstudio.ghost.common.game.NetworkMatch;
import com.boxtrotstudio.ghost.game.match.entities.PlayableEntity;
import com.boxtrotstudio.ghost.game.queue.Queues;

import java.util.Random;

public class CasualGame implements Game {
    private static final String[] MAPS = new String[] {
            "test"
    };
    private static final Random RANDOM = new Random();

    @Override
    public Queues getQueue() {
        return Queues.WEAPONSELECT;
    }

    @Override
    public void onServerStart() { }

    @Override
    public void onServerStop() { }

    @Override
    public short getPlayersPerMatch() {
        return 2;
    }

    @Override
    public void onMatchPreSetup(NetworkMatch activeMatch) { }

    @Override
    public void onMatchSetup(NetworkMatch activeMatch) {
        for (PlayableEntity p : activeMatch.getPlayers()) {
            p.setLives((byte) 3);
        }
    }

    @Override
    public String getMapName() {
        return MAPS[RANDOM.nextInt(MAPS.length)];
    }
}
