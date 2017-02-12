package com.boxtrotstudio.ghost.common.game.gamemodes.impl;

import com.boxtrotstudio.ghost.common.game.NetworkMatch;
import com.boxtrotstudio.ghost.game.team.Team;
import com.boxtrotstudio.ghost.network.Server;
import com.boxtrotstudio.ghost.utils.Condition;

public class TeamDeathMatch extends NetworkMatch {
    public TeamDeathMatch(Team team1, Team team2, Server server) {
        super(team1, team2, server);
    }

    /*
    if (team1.isTeamDead() && !team2.isTeamDead()) {
        end(team2);
    } else if (!team1.isTeamDead() && team2.isTeamDead()) {
        end(team1);
    } else if (team1.isTeamDead()) { //team2.isTeamDead() is always true at this point in the elseif
        end(null);
    }
    */
    @Override
    protected void stage() {
        when(() -> team1.isTeamDead() && !team2.isTeamDead()).execute(() -> end(team1));

        when(() -> !team1.isTeamDead() && team2.isTeamDead()).execute(() -> end(team2));

        when(() -> team1.isTeamDead() && team2.isTeamDead()).execute(() -> end(null));
    }
}
