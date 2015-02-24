package me.eddiep.ghost.server.game;

import me.eddiep.ghost.server.network.Player;

public class Match {
    private Team team1;
    private Team team2;
    private boolean started;

    public Team createTeam1(Player... players) {
        team1 = new Team(1, players);
        return team1;
    }

    public Team createTeam2(Player... players) {
        team2 = new Team(2, players);
        return team2;
    }

    public Team getTeam1() {
        return team1;
    }

    public Team getTeam2() {
        return team2;
    }

    private void start() {
        started = true;

        //Reset ready stat for all players
        for (Player p : team1.getTeamMembers()) {
            p.setReady(false);
        }

        for (Player p : team2.getTeamMembers()) {
            p.setReady(false);
        }
    }

    public void tick() {
        if (!started) {
            if (team1.isTeamReady() && team2.isTeamReady()) {
                start();
            }
        }
    }
}