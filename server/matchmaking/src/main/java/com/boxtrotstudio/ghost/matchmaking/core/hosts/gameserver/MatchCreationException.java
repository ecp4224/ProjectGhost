package com.boxtrotstudio.ghost.matchmaking.core.hosts.gameserver;

public class MatchCreationException extends Throwable {
    public MatchCreationException(Throwable cause) {
        super(cause);
    }

    public MatchCreationException(String s) {
        super(s);
    }
}
