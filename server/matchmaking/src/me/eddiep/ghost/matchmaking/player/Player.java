package me.eddiep.ghost.matchmaking.player;

import me.eddiep.ghost.matchmaking.TcpClient;
import me.eddiep.ghost.matchmaking.queue.PlayerQueue;
import me.eddiep.ghost.network.User;
import me.eddiep.ghost.network.sql.PlayerData;

import java.util.UUID;

public class Player implements User<TcpClient> {

    private PlayerQueue queue;

    public Player(String username, UUID session, PlayerData sqlData) {
        //TODO Construct
    }

    public boolean isInQueue() {
        return false;
    }

    @Override
    public UUID getSession() {
        return null;
    }

    @Override
    public TcpClient getClient() {
        return null;
    }

    @Override
    public void setClient(TcpClient client) {

    }

    @Override
    public boolean isConnected() {
        return false;
    }

    @Override
    public void respondToRequest(int id, boolean value) {

    }

    @Override
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
