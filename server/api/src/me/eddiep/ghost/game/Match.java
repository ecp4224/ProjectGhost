package me.eddiep.ghost.game;

import me.eddiep.ghost.game.queue.Queues;
import me.eddiep.ghost.game.team.OfflineTeam;

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
