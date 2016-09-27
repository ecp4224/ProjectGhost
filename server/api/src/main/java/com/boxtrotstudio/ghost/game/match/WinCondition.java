package com.boxtrotstudio.ghost.game.match;

import com.boxtrotstudio.ghost.game.team.Team;

public interface WinCondition {
    /**
     * Checks the win state of a match provided in the parameter. If a null value is returned,
     * then the match has not ended yet, otherwise the winner is returned
     * @param match The match to check
     * @return The winner of the match or null if the match has not ended yet
     */
    Team checkWinState(LiveMatch match);

    /**
     * Checks the win state of a match provided in the parameter. This function is only called
     * when time has run out in a timed match. If a null value is returned, then the match
     * will go into overtime, otherwise the winner should be returned
     * @param match The match where time has ended
     * @return The winner of the match or null if the match should go into overtime
     */
    Team checkTimedWinState(LiveMatch match);

    /**
     * This method is invoked when overtime is triggered on a LiveMatch.
     * @param match The match where overtime was triggered
     */
    void overtimeTriggered(LiveMatch match);
}
