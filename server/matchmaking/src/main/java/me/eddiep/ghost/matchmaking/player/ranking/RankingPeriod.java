package me.eddiep.ghost.matchmaking.player.ranking;

import me.eddiep.ghost.utils.Global;
import org.bson.Document;

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

    public static RankingPeriod fromDocument(Document doc) {
        return Global.GSON.fromJson(doc.toJson(), RankingPeriod.class);
    }

    public List<RankedGame> getGames() {
        return games;
    }
}
