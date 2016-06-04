package com.boxtrotstudio.ghost.matchmaking.player.ranking;

import java.util.List;

/**
 * Represents the games a player has played during a season
 */
public class RankingPeriod {
    private List<RankedGame> games;

    public static RankingPeriod empty() {
        return new RankingPeriod();
    }

    private RankingPeriod() { }

    public boolean hasPlayed() {
        return games.size() > 0;
    }

    public List<RankedGame> getGames() {
        return games;
    }

    public void addGame(RankedGame game) {
        this.games.add(game);
    }
}
