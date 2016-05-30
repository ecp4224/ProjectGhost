package com.boxtrotstudio.ghost.matchmaking.queue;

import com.boxtrotstudio.ghost.game.queue.Queues;
import com.boxtrotstudio.ghost.matchmaking.network.gameserver.Stream;
import com.boxtrotstudio.ghost.matchmaking.player.Player;

public interface PlayerQueue {

    /**
     * Add a playable instance to this PlayerQueue. When the playable is added, that playable should be processed for
     * possible matches with other players in the PlayerQueue
     * @param player The playable to add to the queue
     */
    void addUserToQueue(Player player);

    /**
     * Remove a playable instance from this PlayerQueue. This playable should no longer be processed for possible
     * matches with other players.
     * @param player The playable to remove
     */
    void removeUserFromQueue(Player player);

    /**
     * Process this PlayerQueue fro possible matches.
     */
    void processQueue();

    long getProcessStartTime();

    /**
     * A description for this PlayerQueue.
     * @return A description
     */
    String description();

    /**
     * The kind of {@link Queues} this PlayerQueue represents
     * @return
     */
    Queues queue();

    /**
     * Get the stream this queue is for
     * @return The {@link Stream} this queue is for
     */
    Stream getStream();

    /**
     * How many allies a playable will get. For example, 0 allies and 1 opponent means it's a 1v1
     * @return The number of allies a playable will get
     */
    int allyCount();

    /**
     * The number of opponents a playable must face. For example, 1 ally and 2 opponents means it's a 2v2.
     * @return The number of opponents a playable must face
     */
    int opponentCount();

    /**
     * The number of players currently waiting in this queue
     * @return The number of players in queue
     */
    int playersInQueue();
}
