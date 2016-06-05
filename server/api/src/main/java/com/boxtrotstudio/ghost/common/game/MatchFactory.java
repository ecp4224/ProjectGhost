package com.boxtrotstudio.ghost.common.game;

public class MatchFactory {

    private static MatchCreator INSTANCE;

    public static void setMatchCreator(MatchCreator creator) {
        if (MatchFactory.INSTANCE != null)
            throw new IllegalArgumentException("Cannot set value of single-ton");

        MatchFactory.INSTANCE = creator;
    }

    public static MatchCreator getCreator() {
        return INSTANCE;
    }
}
