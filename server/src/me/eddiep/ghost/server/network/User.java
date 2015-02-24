package me.eddiep.ghost.server.network;

import java.util.UUID;

public class User {
    private String username;
    private UUID session;
    private Client client;
    private boolean isInQueue;

    static User createUser(String username) {
        User user = new User();
        user.username = username;
        do {
            user.session = UUID.randomUUID();
        } while (UserFactory.findUserByUUID(user.session) != null);

        return user;
    }

    private User() { }

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

    public void setInQueue(boolean inQueue) {
        this.isInQueue = inQueue;
    }
}
