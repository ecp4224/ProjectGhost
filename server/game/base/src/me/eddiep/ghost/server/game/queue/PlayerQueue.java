package me.eddiep.ghost.server.game.queue;

import me.eddiep.ghost.server.game.Game;
import me.eddiep.ghost.server.game.entities.Player;

public interface PlayerQueue {

    /**
     * Add a player instance to this PlayerQueue. When the player is added, that player should be processed for
     * possible matches with other players in the PlayerQueue
     * @param player The player to add to the queue
     */
    public void addUserToQueue(Player player);

    /**
     * Remove a player instance from this PlayerQueue. This player should no longer be processed for possible
     * matches with other players.
     * @param player The player to remove
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
     * How many allies a player will get. For example, 0 allies and 1 opponent means it's a 1v1
     * @return The number of allies a player will get
     */
    public int allyCount();

    /**
     * The number of opponents a player must face. For example, 1 ally and 2 opponents means it's a 2v2.
     * @return The number of opponents a player must face
     */
    public int opponentCount();

    /**
     * Get the owner of this {@link me.eddiep.ghost.server.game.queue.PlayerQueue}
     * @return The owner
     */
    public Game owner();
}
