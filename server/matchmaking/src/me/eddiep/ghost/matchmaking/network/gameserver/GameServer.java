package me.eddiep.ghost.matchmaking.network.gameserver;

import me.eddiep.ghost.game.queue.Queues;
import me.eddiep.ghost.matchmaking.network.GameServerClient;

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
}
