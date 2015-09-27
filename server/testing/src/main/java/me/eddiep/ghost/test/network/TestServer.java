package me.eddiep.ghost.test.network;

import me.eddiep.ghost.common.BaseServerConfig;
import me.eddiep.ghost.common.network.BasePlayerClient;
import me.eddiep.ghost.common.network.BaseServer;

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
