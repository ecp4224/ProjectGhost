package com.boxtrotstudio.ghost.game.match.states;

import com.boxtrotstudio.ghost.game.match.LiveMatch;
import com.boxtrotstudio.ghost.game.match.WinCondition;
import com.boxtrotstudio.ghost.game.team.Team;

public class ScoreState implements WinCondition {
    private int scoreCap = 1;

    public ScoreState() { }

    public ScoreState(int scoreCap) {
        this.scoreCap = scoreCap;
    }

    @Override
    public Team checkWinState(LiveMatch match) {
        if (match.getTeam1().getScore() == scoreCap)
            return match.getTeam1();
        else if (match.getTeam2().getScore() == scoreCap)
            return match.getTeam2();
        return null;
    }

    @Override
    public Team checkTimedWinState(LiveMatch match) {
        if (match.getTeam1().getScore() > match.getTeam2().getScore())
            return match.getTeam1();
        else if (match.getTeam2().getScore() > match.getTeam1().getScore())
            return match.getTeam2();
        return null;
    }

    @Override
    public void overtimeTriggered(LiveMatch match) {
        scoreCap = 1;
        match.getTeam1().setScore(0);
        match.getTeam2().setScore(0);
    }
}
