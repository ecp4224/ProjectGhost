package com.boxtrotstudio.ghost.client.core.game.timeline;

public class OfflineTeam {
    private String[] usernames;
    private Long[] playerIds;
    private int teamNumber;

    private OfflineTeam() { }

    public String[] getUsernames() {
        return usernames;
    }

    public Long[] getPlayerIds() {
        return playerIds;
    }

    public int getTeamLength() {
        return usernames.length;
    }

    public int getTeamNumber() {
        return teamNumber;
    }

    public boolean containsName(String name) {
        for (String n : usernames) {
            if (n.equals(name))
                return true;
        }
        return false;
    }

    public boolean containsID(long id) {
        for (long pID : playerIds) {
            if (id == pID)
                return true;
        }
        return false;
    }
}
