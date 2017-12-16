package com.boxtrotstudio.ghost.gameserver;

import com.boxtrotstudio.ghost.gameserver.api.GameServer;

public class Main {
    public static void main(String[] args) {
        try {
            GameServer.startServer();
        } catch (Exception e) {
            e.printStackTrace(); //5xnj40
                                 //^ The asshole that almost killed me
        }
    }
}
