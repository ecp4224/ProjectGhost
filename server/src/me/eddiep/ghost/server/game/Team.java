package me.eddiep.ghost.server.game;

import me.eddiep.ghost.server.network.Player;

import java.util.List;

public class Team {
    private Player[] members;
    private int teamNumber;

    public Team(int teamNumber, Player... players) {
        members = players;
        this.teamNumber = teamNumber;
    }

    public Team(int teamNumber, List<Player> players) {
        members = players.toArray(new Player[players.size()]);
        this.teamNumber = teamNumber;
    }

    public boolean isTeamDead() {
        for (Player p : members) {
            if (!p.isDead())
                return false;
        }
        return true;
    }

    public boolean isTeamAlive() {
        return !isTeamDead();
    }

    public int getTeamLength() {
        return members.length;
    }

    public int getTeamNumber() {
        return teamNumber;
    }

    public Player[] getTeamMembers() {
        return members;
    }

    public boolean isAlly(Player p) {
        for (Player member : members) {
            if (p.getSession().equals(member.getSession()))
                return true;
        }
        return false;
    }

    public boolean isTeamReady() {
        for (Player p : members) {
            if (!p.isReady())
                return false;
        }
        return true;
    }
}
