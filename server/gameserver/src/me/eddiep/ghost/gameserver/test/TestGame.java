package me.eddiep.ghost.gameserver.test;

import me.eddiep.ghost.game.queue.Queues;
import me.eddiep.ghost.gameserver.api.game.Game;
import me.eddiep.ghost.gameserver.api.network.NetworkMatch;

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

    }
}
