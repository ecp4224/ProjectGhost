package me.eddiep.ghost.client.core.timeline;

public class MatchHistory  {

    private long id;
    private OfflineTeam team1, team2;
    private int winningTeam, losingTeam;
    private long matchStarted, matchEnded;
    private Timeline timeline;
    private byte queue;

    private MatchHistory() { }

    public Timeline getTimeline() {
        return timeline;
    }

    public long getID() {
        return id;
    }

    public OfflineTeam team1() {
        return team1;
    }

    public OfflineTeam team2() {
        return team2;
    }

    public OfflineTeam winningTeam() {
        return team1.getTeamNumber() == winningTeam ? team1 : team2.getTeamNumber() == winningTeam ? team2 : null;
    }

    public OfflineTeam losingTeam() {
        return team1.getTeamNumber() == losingTeam ? team1 : team2.getTeamNumber() == losingTeam ? team2 : null;
    }

    public long getMatchStarted() {
        return matchStarted;
    }

    public long getMatchEnded() {
        return matchEnded;
    }
}
