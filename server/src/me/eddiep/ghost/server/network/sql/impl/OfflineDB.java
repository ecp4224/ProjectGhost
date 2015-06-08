package me.eddiep.ghost.server.network.sql.impl;

import me.eddiep.ghost.server.game.Match;
import me.eddiep.ghost.server.game.stats.MatchHistory;
import me.eddiep.ghost.server.network.sql.PlayerData;
import me.eddiep.ghost.server.network.sql.PlayerUpdate;
import me.eddiep.ghost.server.network.sql.SQL;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents an offline database instance.
 *
 * This database stores no data and will always return null on lookups. However, this database will
 * always accept login info and return a new {@link me.eddiep.ghost.server.network.sql.PlayerData} for each
 * login
 */
public class OfflineDB implements SQL {

    @Override
    public void loadAndSetup() { }

    @Override
    public void storePlayerData(PlayerData data) { }

    @Override
    public void updatePlayerData(PlayerUpdate data) { }

    @Override
    public void bulkUpdate(PlayerUpdate[] updates) { }

    @Override
    public PlayerData fetchPlayerData(String username, String password) {
        return new PlayerData(username, username);
    }

    @Override
    public PlayerData[] fetchPlayerStats(long... id) {
        return new PlayerData[0];
    }

    @Override
    public PlayerData fetchPlayerStat(long id) {
        return null;
    }

    @Override
    public List<PlayerData> fetchPlayerStats(long min, long max) {
        return new ArrayList<PlayerData>();
    }

    @Override
    public long getPlayerCount() {
        return 0;
    }

    @Override
    public boolean createAccount(String username, String password) {
        return true;
    }

    @Override
    public boolean usernameExists(String username) {
        return false;
    }

    @Override
    public boolean displayNameExist(String displayName) {
        return false;
    }

    @Override
    public void saveMatch(MatchHistory history) { }

    @Override
    public long getStoredMatchCount() {
        return 0;
    }

    @Override
    public Match fetchMatch(long id) {
        return null;
    }
}
