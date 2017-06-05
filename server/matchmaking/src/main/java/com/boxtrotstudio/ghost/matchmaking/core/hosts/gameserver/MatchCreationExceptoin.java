package com.boxtrotstudio.ghost.matchmaking.core.hosts.gameserver;

public class MatchCreationExceptoin extends Throwable {
    public MatchCreationExceptoin(Throwable cause) {
        super(cause);
    }

    public MatchCreationExceptoin(String s) {
        super(s);
    }
}
