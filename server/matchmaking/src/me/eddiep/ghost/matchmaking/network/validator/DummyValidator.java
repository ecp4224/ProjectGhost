package me.eddiep.ghost.matchmaking.network.validator;

import me.eddiep.ghost.network.sql.PlayerData;
import me.eddiep.ghost.utils.Global;

public class DummyValidator implements Validator {
    @Override
    public PlayerData validateLogin(String secret) {
        //Always validate secret with new PlayerData object

        String username = "player-" + Global.RANDOM.nextLong();
        return new PlayerData(username, username);
    }
}
