package me.eddiep.ghost.test.network;

import me.eddiep.ghost.common.game.Player;
import me.eddiep.ghost.common.network.BasePlayerClient;
import me.eddiep.ghost.common.network.BaseServer;
import me.eddiep.ghost.test.game.TestPlayer;

import java.io.IOException;
import java.net.Socket;

public class TestClient extends BasePlayerClient {
    public TestClient(Player player, Socket socket, BaseServer server) throws IOException {
        super(player, socket, server);
    }

    public TestPlayer getTestPlayer() {
        return (TestPlayer)getPlayer();
    }
}
