package com.boxtrotstudio.ghost.matchmaking.core.hosts.gameserver;

import com.boxtrotstudio.ghost.game.queue.Queues;
import com.boxtrotstudio.ghost.matchmaking.Main;
import com.boxtrotstudio.ghost.matchmaking.network.GameServerClient;
import com.boxtrotstudio.ghost.matchmaking.network.database.Database;
import com.boxtrotstudio.ghost.matchmaking.network.packets.CreateMatchPacket;
import com.boxtrotstudio.ghost.matchmaking.network.packets.GameServerStreamUpdatePacket;
import com.boxtrotstudio.ghost.matchmaking.network.packets.MatchRedirectPacket;
import com.boxtrotstudio.ghost.matchmaking.player.Player;
import net.gpedro.integrations.slack.SlackMessage;

import java.io.IOException;
import java.util.UUID;

public class GameServer {
    private transient GameServerClient client;
    private GameServerConfiguration config;
    private long timePerTick;
    private boolean isFull;
    private short matchCount;
    private short playerCount;
    private UUID id;
    private String ip;
    private int port;

    GameServer(GameServerClient client, GameServerConfiguration configuration) {
        this.client = client;
        this.config = configuration;
        this.id = UUID.randomUUID();
        this.ip = client.getIpAddress().getCanonicalHostName();
    }

    public UUID getID() {
        return id;
    }

    public GameServerClient getClient() {
        return client;
    }

    public GameServerConfiguration getConfig() {
        return config;
    }

    public void disconnect() {
        GameServerFactory.disconnect(this);

        System.err.println("[SERVER] GameServer " + id.toString() + " from group " + config.getInternalGroup() + " has disconnected!");

        Main.SLACK_API.call(new SlackMessage("Gameserver " + id + " from group " + config.getInternalGroup() + " has disconnected."));

        client = null;
        config = null;
    }

    public String getIp() {
        return ip;
    }

    public int getPort() {
        return port;
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

    public void createMatchFor(Queues queues, Player[] team1, Player[] team2) throws IOException, MatchCreationException {
        //TODO This sends a CreateMatchPacket to this server containing the matchmaking session keys for each player
        //TODO The clients should connect to the game server with these session keys
        long id = Database.getNextID();
        CreateMatchPacket packet = new CreateMatchPacket(client);
        packet.writePacket(queues, id, team1, team2);

        try {
            if (client.isOk(10000L)) { //Timeout in 10 seconds
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
            } else {
                System.err.println("Server " + client.getGameServer().getID() + " refused our match!");
                throw new MatchCreationException("Server refused!");
            }
        } catch (InterruptedException e) {
            System.err.println("Server: " + client.getGameServer().getID() + " is not responding!");
            throw new MatchCreationException(e);
        }
    }

    void setConfig(GameServerConfiguration config) {
        if (this.config.getStream() != config.getStream()) {
            GameServerStreamUpdatePacket packet = new GameServerStreamUpdatePacket(client);
            try {
                packet.writePacket(config.getStream());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        this.config = config;
    }

    public void setStream(Stream stream) {
        config.setStream(stream);
    }

    public void setPort(int port) {
        this.port = port;
    }
}
