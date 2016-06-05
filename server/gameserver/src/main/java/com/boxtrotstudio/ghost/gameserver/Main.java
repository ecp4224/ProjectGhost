package com.boxtrotstudio.ghost.gameserver;

import com.boxtrotstudio.ghost.gameserver.api.GameServer;

import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        try {
            GameServer.startServer();
        } catch (IOException e) {
            e.printStackTrace(); //5xnj40
                                 //^ The asshole that almost killed me
        }
    }
}
