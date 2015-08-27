package me.eddiep.ghost.junit.helpers;

import me.eddiep.ghost.game.team.Team;
import me.eddiep.ghost.network.Server;

public class TestServer extends Server {

    private TestServer() { }

    public static TestServer createServer() {
        TestServer server = new TestServer();
        server.start();
        return server;
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    public TestMatch createTestMatch(int teamSize) {
        TestPlayableEntity[] team1 = new TestPlayableEntity[teamSize];
        TestPlayableEntity[] team2 = new TestPlayableEntity[teamSize];

        for (int i = 0; i < team1.length; i++) {
            team1[i] = new TestPlayableEntity("1_" + i);
            team2[i] = new TestPlayableEntity("2_" + i);
        }

        Team team_1 = new Team(1, team1);
        Team team_2 = new Team(2, team2);

        TestMatch match = new TestMatch(team_1, team_2, this);
        TestWorld world = new TestWorld(match);
        match.setWorld(world);
        match.setup();

        return match;
    }
}
