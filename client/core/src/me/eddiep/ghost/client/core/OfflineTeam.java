package me.eddiep.ghost.client.core;

import java.util.Arrays;
import java.util.List;

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

}
