package me.eddiep.ghost.gameserver.casual2v2;

import me.eddiep.ghost.common.game.NetworkMatch;
import me.eddiep.ghost.game.match.entities.PlayableEntity;
import me.eddiep.ghost.game.queue.Queues;
import me.eddiep.ghost.gameserver.api.game.Game;

import java.util.Random;

public class Casual2v2Game implements Game {
    private static final String[] MAPS = new String[] {
            "test"
    };
    private static final Random RANDOM = new Random();

    @Override
    public Queues getQueue() {
        return Queues.TWO_V_TWO;
    }

    @Override
    public void onServerStart() { }

    @Override
    public void onServerStop() { }

    @Override
    public short getPlayersPerMatch() {
        return 4;
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
