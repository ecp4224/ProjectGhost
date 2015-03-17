package me.eddiep.ghost.server.network.sql;

public interface SQL {

    public void loadAndSetup();

    public void storePlayerData(PlayerData data);

    public void updatePlayerData(PlayerData data);

    public PlayerData fetchPlayerData(String username, String password);

    public void createAccount(String username, String password);
}
