package com.boxtrotstudio.ghost.network.sql;

import com.boxtrotstudio.ghost.game.match.Match;
import com.boxtrotstudio.ghost.game.match.stats.MatchHistory;

import java.util.List;

public interface SQL {

    void loadAndSetup();

    void storePlayerData(PlayerData data);

    void updatePlayerData(PlayerUpdate data);

    void bulkUpdate(PlayerUpdate[] updates);

    PlayerData fetchPlayerData(String username, String password);

    PlayerData[] fetchPlayerStats(long... id);

    PlayerData fetchPlayerStat(long id);

    List<PlayerData> fetchPlayerStats(long min, long max);

    long getPlayerCount();

    boolean createAccount(String username, String password);

    boolean usernameExists(String username);

    boolean displayNameExist(String displayName);

    void saveMatch(MatchHistory history);

    long getStoredMatchCount();

    Match fetchMatch(long id);
}
