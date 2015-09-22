package me.eddiep.ghost.matchmaking.network.gameserver;

public class OfflineGameServer {
    private GameServerConfiguration config;
    private GameServer server;
    private long id;

    OfflineGameServer(GameServerConfiguration config, long id) {
        this.config = config;
        this.id = id;
    }

    OfflineGameServer(GameServerConfiguration config, GameServer server) {
        this.config = config;
        this.server = server;
        this.id = server.getID();
    }

    OfflineGameServer(GameServer server) {
        this.config = server.getConfig();
        this.server = server;
        this.id = server.getID();
    }

    public boolean isConnected() {
        return server != null;
    }

    public GameServer getServer() {
        return server;
    }

    public GameServerConfiguration getConfig() {
        return config;
    }

    public long getID() {
        return id;
    }
}
