package me.eddiep.ghost.server.game;

import me.eddiep.ghost.server.network.Player;
import me.eddiep.ghost.server.packet.impl.MatchFoundPacket;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class Match {
    /*private Team team1;
    private Team team2;*/
    private Player player1;
    private Player player2;
    private boolean started;
    private static final Random RANDOM = new Random();
/*
    public Team createTeam1(Player... players) {
        team1 = new Team(1, players);
        return team1;
    }

    public Team createTeam2(Player... players) {
        team2 = new Team(2, players);
        return team2;
    }*/

    public Player getPlayer1() {
        return player1;
    }

    public Player getPlayer2() {
        return player2;
    }

    private void start() {
        started = true;

        player1.setReady(false);
        player2.setReady(false);

        short p1X = (short) RANDOM.nextInt(512);
        short p1Y = (short) RANDOM.nextInt(720);

        short p2X = (short) (RANDOM.nextInt(512) + 512);
        short p2Y = (short)RANDOM.nextInt(720);

        MatchFoundPacket packet1 = new MatchFoundPacket(player1.getClient());
        MatchFoundPacket packet2 = new MatchFoundPacket(player2.getClient());
        try {
            packet1.writePacket(player2.getClient(), p1X, p1Y);
            packet2.writePacket(player1.getClient(), p2X, p2Y);
        } catch (IOException e) {
            e.printStackTrace();
        }


        /*//Reset ready stat for all players
        for (Player p : team1.getTeamMembers()) {
            p.setReady(false);
        }

        for (Player p : team2.getTeamMembers()) {
            p.setReady(false);
        }

        for (int i = 0; i < team1.getTeamLength(); i++) {
            short p1X = (short) RANDOM.nextInt(512);
            short p1Y = (short) RANDOM.nextInt(720);

            short p2X = (short) (RANDOM.nextInt(512) + 512);
            short P2Y = (short)RANDOM.nextInt(720);

            Player p1 = team1.getTeamMembers()[i];
            Player p2 = null;
            if (i < team2.getTeamLength())
                p2 = team2.getTeamMembers()[i];

            MatchFoundPacket packet1 = new MatchFoundPacket(p1.getClient());
            MatchFoundPacket packet2 = null;
            if (p2 != null)
                packet2 = new MatchFoundPacket(p2.getClient());

            packet1.writePacket()
        }*/
    }

    public void tick() {
        if (!started) {
            if (player1.isReady() && player2.isReady()) {
                start();
            }
        }
    }
}