package com.boxtrotstudio.ghost.network.validate;

import com.boxtrotstudio.ghost.network.sql.PlayerData;

public interface Validator {

    PlayerData validate(String session);
}
