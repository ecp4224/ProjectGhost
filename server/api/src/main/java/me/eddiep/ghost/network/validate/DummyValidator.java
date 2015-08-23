package me.eddiep.ghost.network.validate;

import me.eddiep.ghost.network.sql.PlayerData;
import me.eddiep.ghost.utils.Global;

public class DummyValidator implements Validator {
    @Override
    public PlayerData validate(String session) {

        String username = "player-" + Global.RANDOM.nextLong();
        return new PlayerData(username, username);
    }
}
