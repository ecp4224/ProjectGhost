package com.boxtrotstudio.ghost.matchmaking.core;

import com.boxtrotstudio.ghost.game.queue.Queues;
import com.boxtrotstudio.ghost.matchmaking.core.hosts.gameserver.Stream;
import com.boxtrotstudio.ghost.matchmaking.player.Player;

import java.io.IOException;

public interface MatchHost {

    boolean createMatch(Player[] team1, Player[] team2, Queues queue, Stream stream) throws IOException;

    int size();

    void scaleUp() throws IOException;

    void scaleDown();
}
