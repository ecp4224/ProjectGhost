package com.boxtrotstudio.ghost.game.match.states;

import com.boxtrotstudio.ghost.game.match.LiveMatch;
import com.boxtrotstudio.ghost.game.match.WinCondition;
import com.boxtrotstudio.ghost.game.match.entities.PlayableEntity;
import com.boxtrotstudio.ghost.game.team.Team;

public class TeamDeathMatch implements WinCondition {
    @Override
    public Team checkWinState(LiveMatch match) {
        if (match.getTeam1().isTeamDead() && !match.getTeam2().isTeamDead()) {
            return match.getTeam2();
        } else if (!match.getTeam1().isTeamDead() && match.getTeam2().isTeamDead()) {
            return match.getTeam1();
        } else if (match.getTeam1().isTeamDead()) { //team2.isTeamDead() is always true at this point in the elseif
            return null; //TODO Handle draw!
        }

        return null;
    }

    @Override
    public Team checkTimedWinState(LiveMatch match) {
        if (match.getTeam1().totalLives() > match.getTeam2().totalLives()) {
            return match.getTeam1();
        } else if (match.getTeam2().totalLives() > match.getTeam1().totalLives()) {
            return match.getTeam2();
        } else {
            return null;
        }
    }

    @Override
    public void overtimeTriggered(LiveMatch match) {
        for (PlayableEntity p : match.getPlayers()) {
            if (!p.isDead()) {
                p.setLives((byte) 1);
            }
        }
    }
}
