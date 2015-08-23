package me.eddiep.ghost.gameserver.test;

import me.eddiep.ghost.gameserver.api.GameServer;
import me.eddiep.ghost.network.sql.impl.MongoDB;
import me.eddiep.ghost.network.sql.impl.OfflineDB;
import me.eddiep.ghost.utils.ArrayHelper;
import me.eddiep.ghost.utils.Global;

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
