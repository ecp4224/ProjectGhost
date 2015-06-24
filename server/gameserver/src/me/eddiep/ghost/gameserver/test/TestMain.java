package me.eddiep.ghost.gameserver.test;

import me.eddiep.ghost.gameserver.api.GameServer;

import java.io.IOException;

public class TestMain {

    public static void main(String[] args) {
        try {
            GameServer.startServer(new TestGame());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
