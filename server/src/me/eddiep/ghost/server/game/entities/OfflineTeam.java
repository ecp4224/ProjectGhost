package me.eddiep.ghost.server.game.entities;

public class OfflineTeam {
    private String[] usernames;
    private long[] playerIds;
    private int teamNumber;

    OfflineTeam(Team team) {
        this.teamNumber = team.getTeamNumber();
        usernames = new String[team.getTeamLength()];
        playerIds = new long[team.getTeamLength()];

        Player[] players = team.getTeamMembers();
        for (int i = 0; i < players.length; i++) {
            usernames[i] = players[i].getUsername();
            playerIds[i] = 0; //TODO Create player ID system
            //playerIds[i] = players[i].getPlayerId();
        }
    }

    public String[] getUsernames() {
        return usernames;
    }

    public long[] getPlayerIds() {
        return playerIds;
    }

    public int getTeamLength() {
        return usernames.length;
    }

    public int getTeamNumber() {
        return teamNumber;
    }

    public boolean isAlly(Player p) {
        for (String username : usernames) {
            if (p.getUsername().equals(username))
                return true;
        }
        return false;
    }
}
