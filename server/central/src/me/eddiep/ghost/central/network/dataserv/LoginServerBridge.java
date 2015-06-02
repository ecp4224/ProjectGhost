package me.eddiep.ghost.central.network.dataserv;

public interface LoginServerBridge {

    /**
     * Connect to the login server and check to see if the session id <b>uuid</b> is valid!
     * @param uuid The session id to check
     * @return True if the session is valid, false otherwise
     */
    public boolean isValidSession(String uuid);

    /**
     * Fetch the playable stats for the playable who owns the session id <b>uuid</b>
     * @param uuid The uuid of the playable
     * @return The playable stats for that playable, or null if the session id is invalid
     */
    public PlayerData fetchPlayerStats(String uuid);

    /**
     * Check whether or not the displayName <b>displayName</b> exists
     * @param displayName The displayName to check
     * @return True if the displayName is already in use, otherwise false
     */
    public boolean doesDisplayNameExists(String displayName);

    /**
     * Request that the login server update the playable stats of a playable who owns the session id <b>uuid</b>
     * @param uuid The session id of the playable to update stats for
     * @param update The new playable stats
     * @return True if the operation was successful, otherwise false
     */
    public boolean updatePlayerStats(String uuid, PlayerData update);
}
