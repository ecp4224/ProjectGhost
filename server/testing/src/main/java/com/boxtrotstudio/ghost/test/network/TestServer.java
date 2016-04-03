package com.boxtrotstudio.ghost.test.network;

import com.boxtrotstudio.ghost.common.BaseServerConfig;
import com.boxtrotstudio.ghost.common.network.BasePlayerClient;
import com.boxtrotstudio.ghost.common.network.BaseServer;

import java.io.IOException;

public class TestServer extends BaseServer {
    public TestServer(BaseServerConfig config) {
        super(config);
    }

    @Override
    public BasePlayerClient createClient() throws IOException {
        return new TestClient(this);
    }
}
