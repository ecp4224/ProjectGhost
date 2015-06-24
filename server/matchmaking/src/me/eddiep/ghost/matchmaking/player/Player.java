package me.eddiep.ghost.matchmaking.player;

import me.eddiep.ghost.matchmaking.network.TcpClient;
import me.eddiep.ghost.matchmaking.queue.PlayerQueue;
import me.eddiep.ghost.network.sql.PlayerData;

import java.util.UUID;

public class Player {

    private PlayerQueue queue;

    public Player(String username, UUID session, PlayerData sqlData) {
        //TODO Construct
    }

    public boolean isInQueue() {
        return queue != null;
    }

    public TcpClient getClient() {
        return null;
    }

    public void setClient(TcpClient client) {

    }

    public boolean isConnected() {
        return false;
    }

    public void respondToRequest(int id, boolean value) {

    }

    public void disconnected() {

    }

    public long getPlayerID() {
        return 0;
    }

    public boolean isInMatch() {
        return false;
    }

    public void setQueue(PlayerQueue queue) {
        this.queue = queue;
    }

    public PlayerQueue getQueue() {
        return queue;
    }

    public String getUsername() {
        return null;
    }
}
