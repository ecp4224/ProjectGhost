package me.eddiep.ghost.matchmaking.network.gameserver;

public class MatchCreationExceptoin extends Throwable {
    public MatchCreationExceptoin(Throwable cause) {
        super(cause);
    }

    public MatchCreationExceptoin(String s) {
        super(s);
    }
}
