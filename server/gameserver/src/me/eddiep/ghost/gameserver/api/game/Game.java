package me.eddiep.ghost.gameserver.api.game;

import me.eddiep.ghost.game.queue.Queues;
import me.eddiep.ghost.gameserver.api.network.ActiveMatch;

public interface Game {

    public Queues getQueue();

    public void onServerStart();

    public void onServerStop();

    public short getPlayersPerMatch();

    void onMatchPreSetup(ActiveMatch activeMatch);

    void onMatchSetup(ActiveMatch activeMatch);
}
