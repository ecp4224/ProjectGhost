package me.eddiep.ghost.matchmaking.network.gameserver;

import me.eddiep.ghost.game.queue.Queues;
import me.eddiep.ghost.matchmaking.network.GameServerClient;
import me.eddiep.ghost.matchmaking.network.database.Database;
import me.eddiep.ghost.matchmaking.network.packets.CreateMatchPacket;
import me.eddiep.ghost.matchmaking.network.packets.GameServerStreamUpdatePacket;
import me.eddiep.ghost.matchmaking.network.packets.MatchRedirectPacket;
import me.eddiep.ghost.matchmaking.player.Player;

import java.io.IOException;

public class GameServer {
    private transient GameServerClient client;
    private GameServerConfiguration config;
    private long timePerTick;
    private boolean isFull;
    private short matchCount;
    private short playerCount;
    private long id;

    GameServer(GameServerClient client, GameServerConfiguration configuration, long id) {
        this.client = client;
        this.config = configuration;
        this.id = id;
    }

    public long getID() {
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

    public void createMatchFor(Queues queues, Player[] team1, Player[] team2) throws IOException, MatchCreationExceptoin {
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
                throw new MatchCreationExceptoin("Server refused!");
            }
        } catch (InterruptedException e) {
            System.err.println("Server: " + client.getGameServer().getID() + " is not responding!");
            throw new MatchCreationExceptoin(e);
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
}
