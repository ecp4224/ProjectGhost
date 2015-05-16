package me.eddiep.ghost.server.game;

import me.eddiep.ghost.server.game.entities.OfflineTeam;
import me.eddiep.ghost.server.game.queue.QueueDescription;

public interface Match {

    public OfflineTeam team1();

    public OfflineTeam team2();

    public OfflineTeam winningTeam();

    public OfflineTeam losingTeam();

    public long getMatchStarted();

    public long getMatchEnded();

    public QueueDescription queueDescription();
}
