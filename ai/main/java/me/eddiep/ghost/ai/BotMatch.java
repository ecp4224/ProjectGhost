package me.eddiep.ghost.ai;

import com.boxtrotstudio.ghost.common.game.NetworkMatch;
import com.boxtrotstudio.ghost.game.team.Team;
import com.boxtrotstudio.ghost.network.Server;

public class BotMatch extends NetworkMatch {
    public BotMatch(Team team1, Team team2, Server server) {
        super(team1, team2, server);
    }

    @Override
    public void end(Team team) {
        super.end(team);

        Trainer.matchEnded(this);
    }

}
