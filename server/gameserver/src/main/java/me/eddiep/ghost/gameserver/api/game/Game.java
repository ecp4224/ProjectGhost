package me.eddiep.ghost.gameserver.api.game;

import me.eddiep.ghost.game.queue.Queues;
import me.eddiep.ghost.gameserver.api.network.NetworkMatch;

public interface Game {

    public Queues getQueue();

    public void onServerStart();

    public void onServerStop();

    public short getPlayersPerMatch();

    void onMatchPreSetup(NetworkMatch activeMatch);

    void onMatchSetup(NetworkMatch activeMatch);
}
