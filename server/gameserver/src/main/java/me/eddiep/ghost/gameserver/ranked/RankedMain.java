package me.eddiep.ghost.gameserver.ranked;

import me.eddiep.ghost.gameserver.api.GameServer;

import java.io.IOException;

public class RankedMain {
    public static void main(String[] args) throws IOException {
        GameServer.startServer(new RankedGame());
    }
}
