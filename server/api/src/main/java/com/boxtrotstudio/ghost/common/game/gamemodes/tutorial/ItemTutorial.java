package com.boxtrotstudio.ghost.common.game.gamemodes.tutorial;

import com.boxtrotstudio.ghost.common.game.NetworkMatch;
import com.boxtrotstudio.ghost.game.team.Team;
import com.boxtrotstudio.ghost.network.Server;

public class ItemTutorial extends NetworkMatch {
    public ItemTutorial(Team team1, Team team2, Server server) {
        super(team1, team2, server);
    }

    @Override
    protected void stage() {

    }
}
