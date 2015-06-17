package me.eddiep.ghost.server.game.queue;

import me.eddiep.ghost.server.game.Game;
import me.eddiep.ghost.server.game.entities.playable.impl.Player;

public interface PlayerQueue {

    /**
     * Add a playable instance to this PlayerQueue. When the playable is added, that playable should be processed for
     * possible matches with other players in the PlayerQueue
     * @param player The playable to add to the queue
     */
    public void addUserToQueue(Player player);

    /**
     * Remove a playable instance from this PlayerQueue. This playable should no longer be processed for possible
     * matches with other players.
     * @param player The playable to remove
     */
    public void removeUserFromQueue(Player player);

    /**
     * Process this PlayerQueue fro possible matches.
     */
    public void processQueue();

    /**
     * Get how many players are waiting in this queue
     * @return The amount of players in this queue
     */
    public int playerCount();

    /**
     * How many allies a playable will get. For example, 0 allies and 1 opponent means it's a 1v1
     * @return The number of allies a playable will get
     */
    public int allyCount();

    /**
     * The number of opponents a playable must face. For example, 1 ally and 2 opponents means it's a 2v2.
     * @return The number of opponents a playable must face
     */
    public int opponentCount();

    /**
     * Get the owner of this {@link me.eddiep.ghost.server.game.queue.PlayerQueue}
     * @return The owner
     */
    public Game owner();
}
