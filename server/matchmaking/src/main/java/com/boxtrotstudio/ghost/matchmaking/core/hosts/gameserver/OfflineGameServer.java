package com.boxtrotstudio.ghost.matchmaking.core.hosts.gameserver;

import java.util.UUID;

public class OfflineGameServer {
    private GameServerConfiguration config;
    private GameServer server;
    private UUID id;

    OfflineGameServer(GameServerConfiguration config, UUID id) {
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

    public UUID getID() {
        return id;
    }
}
