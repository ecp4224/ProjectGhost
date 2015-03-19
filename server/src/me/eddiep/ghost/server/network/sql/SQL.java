package me.eddiep.ghost.server.network.sql;

public interface SQL {

    public void loadAndSetup();

    public void storePlayerData(PlayerData data);

    public void updatePlayerData(PlayerUpdate data);

    public PlayerData fetchPlayerData(String username, String password);

    public PlayerData[] fetchPlayerStats(long... id);

    public PlayerData fetchPlayerStat(long id);

    public boolean createAccount(String username, String password);

    public boolean usernameExists(String username);

    public boolean displayNameExist(String displayName);
}
