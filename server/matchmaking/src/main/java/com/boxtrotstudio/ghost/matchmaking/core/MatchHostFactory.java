package com.boxtrotstudio.ghost.matchmaking.core;

import com.boxtrotstudio.ghost.matchmaking.Main;
import com.boxtrotstudio.ghost.matchmaking.core.hosts.BoxtrotHost;

public class MatchHostFactory {

    private static MatchHost INSTANCE;

    public static MatchHost getHost() {
        if (INSTANCE != null)
            return INSTANCE;

        String _class = Main.getServer().getConfig().matchHostClassPath();
        Class<? extends MatchHost> clazz;
        try {
            clazz = (Class<? extends MatchHost>) Class.forName(_class);

            INSTANCE = clazz.newInstance();
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
            System.err.println("Failed to create host provider " + _class);
            System.err.println("Defaulting to BoxtrotHost Provider!");
            INSTANCE = new BoxtrotHost();
        }

        return INSTANCE;
    }
}
