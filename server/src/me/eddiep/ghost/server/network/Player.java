package me.eddiep.ghost.server.network;

import java.util.UUID;

public class Player {
    private String username;
    private UUID session;
    private Client client;
    private boolean isInQueue;
    private boolean isInMatch;
    private boolean isDead;
    private boolean isReady;

    static Player createPlayer(String username) {
        Player player = new Player();
        player.username = username;
        do {
            player.session = UUID.randomUUID();
        } while (PlayerFactory.findPlayerByUUID(player.session) != null);

        return player;
    }

    private Player() { }

    public String getUsername() {
        return username;
    }

    public UUID getSession() {
        return session;
    }

    public Client getClient() {
        return client;
    }

    void setClient(Client c) {
        this.client = c;
    }

    public boolean isInQueue() {
        return isInQueue;
    }

    public boolean isInMatch() {
        return isInMatch;
    }

    public boolean isDead() {
        return isDead;
    }

    public void isDead(boolean value) {
        this.isDead = value;
        //TODO Do things
    }

    void setInMatch(boolean value) {
        this.isInMatch = value;
    }

    public void setInQueue(boolean inQueue) {
        this.isInQueue = inQueue;
    }

    public boolean isReady() {
        return isReady;
    }

    public void setReady(boolean isReady) {
        this.isReady = isReady;
    }
}
