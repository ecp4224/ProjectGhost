package me.eddiep.ghost.network.validate;

import me.eddiep.ghost.network.sql.PlayerData;

public interface Validator {

    PlayerData validate(String session);
}
