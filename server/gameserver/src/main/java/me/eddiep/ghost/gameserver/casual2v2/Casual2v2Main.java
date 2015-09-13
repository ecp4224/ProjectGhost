package me.eddiep.ghost.gameserver.casual2v2;

import me.eddiep.ghost.gameserver.api.GameServer;

import java.io.IOException;

public class Casual2v2Main {
    public static void main(String[] args) throws IOException {
        GameServer.startServer(new Casual2v2Game());
    }
}
