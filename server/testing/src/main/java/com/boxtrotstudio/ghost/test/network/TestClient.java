package com.boxtrotstudio.ghost.test.network;

import com.boxtrotstudio.ghost.common.network.BasePlayerClient;
import com.boxtrotstudio.ghost.common.network.BaseServer;
import com.boxtrotstudio.ghost.test.game.TestPlayer;

import java.io.IOException;

public class TestClient extends BasePlayerClient {

    public TestClient(BaseServer server) throws IOException {
        super(server);
    }

    public TestPlayer getTestPlayer() {
        return (TestPlayer)getPlayer();
    }
}
