package me.eddiep.ghost.server.game;

import me.eddiep.ghost.server.game.entities.Team;

public interface Match {

    public int getID();

    public Team getTeam1();

    public Team getTeam2();

    public Team getWinningTeam();

    public Team getLosingTeam();

    public long getMatchStarted();

    public long getMatchEnded();
}
