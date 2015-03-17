package me.eddiep.ghost.server.network.sql.impl;

import me.eddiep.ghost.server.network.sql.PlayerData;
import me.eddiep.ghost.server.network.sql.SQL;

public class MongoDB implements SQL {
    @Override
    public void loadAndSetup() {

    }

    @Override
    public void storePlayerData(PlayerData data) {

    }

    @Override
    public void updatePlayerData(PlayerData data) {

    }

    @Override
    public PlayerData fetchPlayerData(String username, String password) {
        return null;
    }

    @Override
    public void createAccount(String username, String password) {

    }
}
