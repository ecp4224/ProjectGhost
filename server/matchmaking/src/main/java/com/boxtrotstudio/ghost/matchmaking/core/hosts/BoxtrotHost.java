package com.boxtrotstudio.ghost.matchmaking.core.hosts;

import com.boxtrotstudio.ghost.game.queue.Queues;
import com.boxtrotstudio.ghost.matchmaking.core.MatchHost;
import com.boxtrotstudio.ghost.matchmaking.core.hosts.gameserver.GameServerFactory;
import com.boxtrotstudio.ghost.matchmaking.core.hosts.gameserver.Stream;
import com.boxtrotstudio.ghost.matchmaking.player.Player;

import java.io.IOException;

public class BoxtrotHost implements MatchHost {
    @Override
    public boolean createMatch(Player[] team1, Player[] team2, Queues queue, Stream stream) throws IOException {
        return GameServerFactory.createMatchFor(queue, team1, team2, stream) != null;
    }

    @Override
    public int size() {
        return GameServerFactory.getConnectedServers().size();
    }

    @Override
    public void scaleUp() throws IOException {
        //TODO Launch more gameservers
    }

    @Override
    public void scaleDown() {
        //TODO Kill empty gameservers
    }
}
