package com.boxtrotstudio.ghost.client.core.game.timeline;

import com.boxtrotstudio.ghost.client.utils.WorldMap;

public class MatchHistory  {

    private long id;
    private OfflineTeam team1, team2;
    private int winningTeam, losingTeam;
    private long matchStarted, matchEnded;
    private Timeline timeline;
    private byte queue;
    private WorldMap map;

    private MatchHistory() { }

    public Timeline getTimeline() {
        return timeline;
    }

    public WorldMap getMap() {
        return map;
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

    public OfflineTeam teamFor(String username) {
        if (team1.containsName(username))
            return team1;
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
