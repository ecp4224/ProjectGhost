package me.eddiep.ghost.server.game.stats;

import me.eddiep.ghost.server.game.ActiveMatch;
import me.eddiep.ghost.server.game.Match;
import me.eddiep.ghost.server.game.team.OfflineTeam;
import me.eddiep.ghost.server.game.entities.playable.impl.Player;
import me.eddiep.ghost.server.game.queue.QueueDescription;

public class MatchHistory implements Match {

    private OfflineTeam team1, team2;
    private int winningTeam, losingTeam;
    private long matchStarted, matchEnded;
    private TrackingMatchStats.FinalizedMatchStats[] playerStats;

    public MatchHistory(ActiveMatch match) {
        this.team1 = match.team1();
        this.team2 = match.team2();
        this.winningTeam = match.winningTeam() == null ? -1 : match.winningTeam().getTeamNumber();
        this.losingTeam = match.losingTeam() == null ? -1 : match.losingTeam().getTeamNumber();
        this.matchStarted = match.getMatchStarted();
        this.matchEnded = match.getMatchEnded();

        playerStats = new TrackingMatchStats.FinalizedMatchStats[team1.getTeamLength() + team2.getTeamLength()];

        int i = 0;
        for (Player p : match.getTeam1().getTeamMembers()) {
            if (match.hasMatchEnded())
                playerStats[i] = p.getTrackingStats().finalized();
            else
                playerStats[i] = p.getTrackingStats().preview();
            i++;
        }

        for (Player p : match.getTeam2().getTeamMembers()) {
            if (match.hasMatchEnded())
                playerStats[i] = p.getTrackingStats().finalized();
            else
                playerStats[i] = p.getTrackingStats().preview();
            i++;
        }
    }

    private MatchHistory() { }

    public TrackingMatchStats.FinalizedMatchStats[] getPlayerMatchStats() {
        return playerStats;
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
        return team1.getTeamNumber() == winningTeam ? team1 : team2.getTeamNumber() == winningTeam ? team2 : null;
    }

    @Override
    public OfflineTeam losingTeam() {
        return team1.getTeamNumber() == losingTeam ? team1 : team2.getTeamNumber() == losingTeam ? team2 : null;
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
    public QueueDescription queueDescription() {
        return null;
    }
}
