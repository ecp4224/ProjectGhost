package me.eddiep.ghost.matchmaking.network.validator;

import me.eddiep.ghost.network.sql.PlayerData;

public interface Validator {
    /**
     * Validate a Client's login with the secret they provided. If the secret is valid, it will return the PlayerData
     * the secret is for
     * @param secret The secret the client used to validate
     * @return The PlayerData the secret is for, or null if the secret is invalid
     */
    public PlayerData validateLogin(String secret);
}
