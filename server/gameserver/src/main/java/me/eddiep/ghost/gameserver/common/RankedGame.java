package me.eddiep.ghost.gameserver.common;

import me.eddiep.ghost.common.game.NetworkMatch;
import me.eddiep.ghost.game.match.entities.PlayableEntity;
import me.eddiep.ghost.game.queue.Queues;
import me.eddiep.ghost.gameserver.api.game.Game;

public class RankedGame implements Game{
    @Override
    public Queues getQueue() {
        return Queues.RANKED;
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
    public void onMatchPreSetup(NetworkMatch activeMatch) {
        activeMatch.shouldSpawnItems(false); //Ranked games should not spawn items
    }

    @Override
    public void onMatchSetup(NetworkMatch activeMatch) {
        for (PlayableEntity p : activeMatch.getPlayers()) {
            p.setLives((byte) 3);
        }
    }

    @Override
    public String getMapName() {
        return "ranked";
    }
}
