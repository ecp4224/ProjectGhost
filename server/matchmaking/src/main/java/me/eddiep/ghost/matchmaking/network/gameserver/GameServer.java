package me.eddiep.ghost.matchmaking.network.gameserver;

import me.eddiep.ghost.game.queue.Queues;
import me.eddiep.ghost.matchmaking.network.GameServerClient;
import me.eddiep.ghost.matchmaking.network.packets.CreateMatchPacket;
import me.eddiep.ghost.matchmaking.network.packets.MatchRedirectPacket;
import me.eddiep.ghost.matchmaking.player.Player;
import me.eddiep.ghost.utils.Global;

import java.io.IOException;

public class GameServer {
    private GameServerClient client;
    private GameServerConfiguration config;
    private long timePerTick;
    private boolean isFull;
    private short matchCount;
    private short playerCount;

    GameServer(GameServerClient client, GameServerConfiguration configuration) {
        this.client = client;
        this.config = configuration;
    }

    public GameServerClient getClient() {
        return client;
    }

    public GameServerConfiguration getConfig() {
        return config;
    }

    public Queues getQueueServing() {
        return Queues.byteToType(config.getQueueServing());
    }

    public void disconnect() {
        GameServerFactory.disconnect(this);

        System.err.println("[SERVER] GameServer " + config.getInternalName() + " has disconnected!");

        client = null;
        config = null;
    }

    public void updateInfo(short playerCount, short matchCount, boolean isFull, long timePerTick) {
        this.playerCount = playerCount;
        this.matchCount = matchCount;
        this.isFull = isFull;
        this.timePerTick = timePerTick;
    }

    public long getTimePerTick() {
        return timePerTick;
    }

    public boolean isFull() {
        return isFull;
    }

    public short getMatchCount() {
        return matchCount;
    }

    public short getPlayerCount() {
        return playerCount;
    }

    public void createMatchFor(Player[] team1, Player[] team2) throws IOException {
        //TODO This sends a CreateMatchPacket to this server containing the matchmaking session keys for each player
        //TODO The clients should connect to the game server with these session keys

        long id = Global.SQL.getStoredMatchCount() + 1;
        CreateMatchPacket packet = new CreateMatchPacket(client);
        packet.writePacket(id, team1, team2);

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        for (Player p : team1) {
            MatchRedirectPacket _packet = new MatchRedirectPacket(p.getClient());
            _packet.writePacket(this);
        }

        for (Player p : team2) {
            MatchRedirectPacket _packet = new MatchRedirectPacket(p.getClient());
            _packet.writePacket(this);
        }
    }
}
