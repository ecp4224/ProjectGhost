package me.eddiep.ghost.gameserver.casual;

import me.eddiep.ghost.gameserver.api.GameServer;

import java.io.IOException;

public class CasualMain {
    public static void main(String[] args) throws IOException {
        GameServer.startServer(new CasualGame());
    }
}
