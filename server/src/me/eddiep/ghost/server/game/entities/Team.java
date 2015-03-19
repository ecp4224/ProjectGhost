package me.eddiep.ghost.server.game.entities;

import me.eddiep.ghost.server.Main;
import me.eddiep.ghost.server.game.Match;
import me.eddiep.ghost.server.network.sql.PlayerUpdate;

import java.util.List;
import java.util.UUID;

public class Team {
    private Player[] members;
    private int teamNumber;

    public Team(int teamNumber, Player... players) {
        members = players;
        this.teamNumber = teamNumber;
    }

    public Team(int teamNumber, UUID... players) {
        Player[] p = new Player[players.length];
        for (int i = 0; i < p.length; i++) {
            Player player;
            if ((player = PlayerFactory.findPlayerByUUID(players[i])) == null) {
                throw new IllegalArgumentException("Invalid UUID!");
            }

            p[i] = player;
        }

        this.members = p;
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

    private OfflineTeam offlineTeam;
    public OfflineTeam offlineTeam() {
        if (offlineTeam == null)
            offlineTeam = new OfflineTeam(this);
        return offlineTeam;
    }

    public void onWin(Match match) {
        for (Player member : members) {
            int val;

            if (member.winHash.containsKey(match.queueType().asByte())) {
                val = member.winHash.get(match.queueType().asByte());
                val++;
                member.winHash.put(match.queueType().asByte(), val);
            } else {
                member.winHash.put(match.queueType().asByte(), 1);
                val = 1;
            }

            member.saveSQLData(match.queueType(), true, val);
        }
    }

    public void onLose(Match match) {
        for (Player member : members) {
            int val;
            if (member.loseHash.containsKey(match.queueType().asByte())) {
                val = member.loseHash.get(match.queueType().asByte());
                val++;
                member.loseHash.put(match.queueType().asByte(), val);
            } else {
                member.loseHash.put(match.queueType().asByte(), 1);
                val = 1;
            }

            member.saveSQLData(match.queueType(), false, val);
        }
    }
}
