package me.eddiep.ghost.matchmaking.player.ranking;

import org.bson.Document;

public class RankedGame {
    double outcome;
    double rank;
    double rd;

    private RankedGame() { }

    public static RankedGame fromDocument(Document document) {
        RankedGame game = new RankedGame();
        game.outcome = document.getDouble("outcome");
        game.rank = document.getDouble("rank");
        game.rd = document.getDouble("rd");

        return game;
    }

}
