package com.boxtrotstudio.ghost.test.game;

import com.boxtrotstudio.ghost.common.game.Player;
import com.boxtrotstudio.ghost.common.game.PlayerFactory;
import com.boxtrotstudio.ghost.network.sql.PlayerData;
import com.boxtrotstudio.ghost.test.game.queue.PlayerQueue;
import com.boxtrotstudio.ghost.test.network.TestClient;

import java.util.UUID;

public class TestPlayer extends Player {
    private PlayerQueue queue;

    protected TestPlayer(String username, String session, PlayerData sqlData) {
        super(username, session, sqlData);
    }

    /**
     * Create a new user with the provided username and SQL Data
     * @param username The username of this playable
     * @param sqlData The SQL data for associated with this playable
     * @return A new {@link Player} object
     */
    static Player createPlayer(String username, PlayerData sqlData) {
        UUID session;
        do {
            session = UUID.randomUUID();
        } while (PlayerFactory.getCreator().findPlayerByUUID(session) != null);

        return new TestPlayer(username, session.toString(), sqlData);
    }

    /**
     * Check whether or not this playable is currently waiting in queue
     * @return True if the playable is waiting in queue, otherwise false
     */
    public boolean isInQueue() {
        return queue != null;
    }

    /**
     * Get the current queue this playable is waiting in
     * @return The current queue this playable is waiting in
     */
    public PlayerQueue getQueue() {
        return queue;
    }

    /**
     * Set the queue this playable is currently waiting in <b>This method should only be called by {@link PlayerQueue} objects!</b>
     * @param queue The queue the playable joined
     */
    public void setQueue(PlayerQueue queue) {
        this.queue = queue;
        super.lastActive = System.currentTimeMillis();
    }

    public TestClient getTestClient() {
        return (TestClient)getClient();
    }

    @Override
    public void disconnected() {
        if (queue != null)
            queue.removeUserFromQueue(this);

        super.disconnected();
    }
}
