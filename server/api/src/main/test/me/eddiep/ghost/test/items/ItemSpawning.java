package me.eddiep.ghost.test.items;

import me.eddiep.ghost.test.helpers.TestMatch;
import me.eddiep.ghost.test.helpers.TestServer;
import org.junit.Test;

public class ItemSpawning {

    @Test
    public void noItemSpawning() {
        TestServer server = TestServer.createServer();
        TestMatch match = server.createTestMatch(1);
        match.start();


    }
}
