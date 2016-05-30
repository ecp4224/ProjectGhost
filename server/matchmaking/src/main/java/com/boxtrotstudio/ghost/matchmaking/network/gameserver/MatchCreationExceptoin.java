package com.boxtrotstudio.ghost.matchmaking.network.gameserver;

public class MatchCreationExceptoin extends Throwable {
    public MatchCreationExceptoin(Throwable cause) {
        super(cause);
    }

    public MatchCreationExceptoin(String s) {
        super(s);
    }
}
