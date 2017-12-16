package com.boxtrotstudio.ghost.common.game;

public class PlayerFactory {

    private static PlayerCreator INSTANCE;

    public static void setPlayerCreator(PlayerCreator creator) {
        if (PlayerFactory.INSTANCE != null)
            throw new IllegalArgumentException("Cannot set value of single-ton");

        PlayerFactory.INSTANCE = creator;
    }

    public static PlayerCreator getCreator() {
        return INSTANCE;
    }
}
