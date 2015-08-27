package me.eddiep.ghost.junit.items;

import me.eddiep.ghost.junit.helpers.TestMatch;
import me.eddiep.ghost.junit.helpers.TestServer;
import org.junit.Test;

public class ItemSpawning {

    @Test
    public void noItemSpawning() {
        TestServer server = TestServer.createServer();
        TestMatch match = server.createTestMatch(1);
        match.start();
    }
}
