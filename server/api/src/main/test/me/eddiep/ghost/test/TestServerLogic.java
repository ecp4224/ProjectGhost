package me.eddiep.ghost.test;

import me.eddiep.ghost.test.helpers.TestMatch;
import me.eddiep.ghost.test.helpers.TestServer;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class TestServerLogic {

    @Test
    public void testStartStop() {
        TestServer server = TestServer.createServer();

        assertTrue("Server is running", server.isRunning());

        server.stop();

        assertTrue("Server stopped", !server.isRunning());
    }

    @Test
    public void testMatchCreation() {
        TestServer server = TestServer.createServer();

        TestMatch match = server.createTestMatch(1);

        assertTrue("Team size is 1", match.getTeam1().getTeamLength() == 1 && match.getTeam2().getTeamLength() == 1);

        match.start();

        assertTrue("Match has started", match.hasMatchStarted());

        match.end(null);

        assertTrue("Match has ended", match.hasMatchEnded());
        assertEquals("Match status message is draw", match.getStatusMessage(), "Draw!");

        server.stop();
    }

    @Test
    public void testWinner() {
        TestServer server = TestServer.createServer();
        TestMatch match = server.createTestMatch(1);
        match.start();

        match.getTeam1().getTeamMembers()[0].kill();

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        assertTrue("Match has ended!", match.hasMatchEnded());

        assertSame("Team 2 won", match.getWinningTeam(), match.getTeam2());

        server.stop();
    }
}
