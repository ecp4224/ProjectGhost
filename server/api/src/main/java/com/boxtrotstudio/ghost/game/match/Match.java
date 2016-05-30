package com.boxtrotstudio.ghost.game.match;

import com.boxtrotstudio.ghost.game.queue.Queues;
import com.boxtrotstudio.ghost.game.team.OfflineTeam;

public interface Match {

    long getID();

    OfflineTeam team1();

    OfflineTeam team2();

    OfflineTeam winningTeam();

    OfflineTeam losingTeam();

    long getMatchStarted();

    long getMatchEnded();

    Queues queueType();
}
