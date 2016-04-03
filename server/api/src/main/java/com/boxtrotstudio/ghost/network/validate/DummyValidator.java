package com.boxtrotstudio.ghost.network.validate;

import com.boxtrotstudio.ghost.utils.Global;
import com.boxtrotstudio.ghost.network.sql.PlayerData;

public class DummyValidator implements Validator {
    @Override
    public PlayerData validate(String session) {
        String username = "player-" + Global.RANDOM.nextLong();
        return new PlayerData(username, username);
    }
}
