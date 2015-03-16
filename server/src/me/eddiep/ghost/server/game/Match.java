package me.eddiep.ghost.server.game;

import me.eddiep.ghost.server.game.entities.OfflineTeam;
import me.eddiep.ghost.server.game.entities.Team;

public interface Match {

    public int getID();

    public OfflineTeam team1();

    public OfflineTeam team2();

    public OfflineTeam winningTeam();

    public OfflineTeam losingTeam();

    public long getMatchStarted();

    public long getMatchEnded();
}
