package me.eddiep.ghost.gameserver.common;

import me.eddiep.ghost.common.game.NetworkMatch;
import me.eddiep.ghost.game.match.abilities.Gun;
import me.eddiep.ghost.game.match.entities.PlayableEntity;
import me.eddiep.ghost.game.queue.Queues;
import me.eddiep.ghost.gameserver.api.game.Game;

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
