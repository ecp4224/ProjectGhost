package me.eddiep.ghost.network.validate;

import me.eddiep.ghost.network.sql.PlayerData;
import me.eddiep.ghost.utils.Global;
import me.eddiep.ghost.utils.WebUtils;

import java.io.IOException;

import static me.eddiep.ghost.utils.Constants.api;

public class LoginServerValidator implements Validator {
    @Override
    public PlayerData validate(String session) {
        try {
            String json = WebUtils.readContentsToString(api("user/info"));

            return Global.GSON.fromJson(json, PlayerData.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
