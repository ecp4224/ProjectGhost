package me.eddiep.ghost.gameserver.api.game;

import me.eddiep.ghost.common.game.NetworkMatch;
import me.eddiep.ghost.game.queue.Queues;

public interface Game {

    public Queues getQueue();

    public void onServerStart();

    public void onServerStop();

    public short getPlayersPerMatch();

    void onMatchPreSetup(NetworkMatch activeMatch);

    void onMatchSetup(NetworkMatch activeMatch);
}
