package me.eddiep.ghost.server.game;

import me.eddiep.ghost.server.game.team.OfflineTeam;
import me.eddiep.ghost.server.game.queue.Queues;

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
