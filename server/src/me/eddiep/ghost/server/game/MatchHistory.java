package me.eddiep.ghost.server.game;

import me.eddiep.ghost.server.game.entities.OfflineTeam;
import me.eddiep.ghost.server.game.queue.Queues;

public class MatchHistory implements Match {

    private int id;
    private OfflineTeam team1, team2, winningTeam, losingTeam;
    private long matchStarted, matchEnded;
    private Queues type;

    MatchHistory(ActiveMatch match) {
        this.id = match.getID();
        this.team1 = match.team1();
        this.team2 = match.team2();
        this.winningTeam = match.winningTeam();
        this.losingTeam = match.losingTeam();
        this.matchStarted = match.getMatchStarted();
        this.matchEnded = match.getMatchEnded();
        this.type = match.queueType();
    }

    @Override
    public int getID() {
        return id;
    }

    @Override
    public OfflineTeam team1() {
        return team1;
    }

    @Override
    public OfflineTeam team2() {
        return team2;
    }

    @Override
    public OfflineTeam winningTeam() {
        return winningTeam;
    }

    @Override
    public OfflineTeam losingTeam() {
        return losingTeam;
    }

    @Override
    public long getMatchStarted() {
        return matchStarted;
    }

    @Override
    public long getMatchEnded() {
        return matchEnded;
    }

    @Override
    public Queues queueType() {
        return type;
    }
}
