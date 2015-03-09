package me.eddiep.ghost.server.game;

import me.eddiep.ghost.server.Main;
import me.eddiep.ghost.server.TcpUdpServer;
import me.eddiep.ghost.server.game.impl.Player;
import me.eddiep.ghost.server.game.util.Vector2f;
import me.eddiep.ghost.server.network.packet.impl.MatchFoundPacket;
import me.eddiep.ghost.server.network.packet.impl.MatchStatusPacket;

import java.io.IOException;

public class Match {
    private Team team1;
    private Team team2;
    private TcpUdpServer server;
    private boolean started;
    private long timeStarted;
    private short lastID = 0;

    public Match(Team team1, Team team2, TcpUdpServer server) {
        this.team1 = team1;
        this.team2 = team2;
        this.server = server;
    }

    public Match(Player player1, Player player2) {
        this(new Team(1, player1), new Team(2, player2), player1.getClient().getServer());
    }

    public Team getTeam1() {
        return team1;
    }

    public Team getTeam2() {
        return team2;
    }

    private void start() {
        started = true;

        timeStarted = System.currentTimeMillis();

        for (Player p : team1.getTeamMembers()) {
            p.setReady(false);
            p.setVisible(false);
            p.resetUpdateTimer();

            MatchStatusPacket packet = new MatchStatusPacket(p.getClient());
            try {
                packet.writePacket(true);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        for (Player p : team2.getTeamMembers()) {
            p.setReady(false);
            p.setVisible(false);
            p.resetUpdateTimer();

            MatchStatusPacket packet = new MatchStatusPacket(p.getClient());
            try {
                packet.writePacket(true);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public Team getTeamFor(Player player) {
        if (team1.isAlly(player))
            return team1;
        else if (team2.isAlly(player))
            return team2;
        return null;
    }

    public void setID(Entity entity) {
        lastID++;
        entity.setID(lastID);
    }

    public void tick() {
        if (!started) {
            if (team1.isTeamReady() && team2.isTeamReady()) {
                start();
            }
        }

        team1.tick();
        team2.tick();

        server.executeNextTick(new Runnable() {
            @Override
            public void run() {
                tick();
            }
        });
    }

    private void spawnEntitiesFor(Player p) throws IOException {
        for (Player toSpawn : team1.getTeamMembers()) {
            if (p == toSpawn)
                continue;

            if (toSpawn.getID() == -1)
                setID(toSpawn);

            p.spawnEntity(toSpawn);
        }

        for (Player toSpawn : team2.getTeamMembers()) {
            if (p == toSpawn)
                continue;

            if (toSpawn.getID() == -1)
                setID(toSpawn);

            p.spawnEntity(toSpawn);
        }
    }

    public void setup() throws IOException {
        for (Player p : team1.getTeamMembers()) {
            float p1X = (float)Main.random(-504, 0);
            float p1Y = (float)Main.random(-350, 350);

            p.setPosition(new Vector2f(p1X, p1Y));
            p.setVelocity(0f, 0f);

            MatchFoundPacket packet = new MatchFoundPacket(p.getClient());
            packet.writePacket(p1X, p1Y);

            p.setMatch(this);
            p.setVisible(true);
        }

        for (Player p : team2.getTeamMembers()) {
            float p1X = (float)Main.random(0, 504);
            float p1Y = (float)Main.random(-350, 350);

            p.setPosition(new Vector2f(p1X, p1Y));
            p.setVelocity(0f, 0f);

            MatchFoundPacket packet = new MatchFoundPacket(p.getClient());
            packet.writePacket(p1X, p1Y);

            p.setMatch(this);
            p.setVisible(true);
        }

        for (Player p : team1.getTeamMembers()) {
            spawnEntitiesFor(p);
        }

        for (Player p : team2.getTeamMembers()) {
            spawnEntitiesFor(p);
        }

        server.executeNextTick(new Runnable() {
            @Override
            public void run() {
                tick();
            }
        });
    }

    public long getTimeElapsed() {
        return System.currentTimeMillis() - timeStarted;
    }

    public boolean hasMatchStarted() {
        return started;
    }
}