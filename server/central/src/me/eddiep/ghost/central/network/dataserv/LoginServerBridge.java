package me.eddiep.ghost.central.network.dataserv;

public interface LoginServerBridge {

    /**
     * Connect to the login server and check to see if the session id <b>uuid</b> is valid!
     * @param uuid The session id to check
     * @return True if the session is valid, false otherwise
     */
    public boolean isValidSession(String uuid);

    /**
     * Fetch the player stats for the player who owns the session id <b>uuid</b>
     * @param uuid The uuid of the player
     * @return The player stats for that player, or null if the session id is invalid
     */
    public PlayerData fetchPlayerStats(String uuid);

    /**
     * Check whether or not the displayName <b>displayName</b> exists
     * @param displayName The displayName to check
     * @return True if the displayName is already in use, otherwise false
     */
    public boolean doesDisplayNameExists(String displayName);

    /**
     * Request that the login server update the player stats of a player who owns the session id <b>uuid</b>
     * @param uuid The session id of the player to update stats for
     * @param update The new player stats
     * @return True if the operation was successful, otherwise false
     */
    public boolean updatePlayerStats(String uuid, PlayerData update);
}
