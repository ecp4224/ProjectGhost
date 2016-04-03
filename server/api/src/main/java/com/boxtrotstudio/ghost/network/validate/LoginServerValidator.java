package com.boxtrotstudio.ghost.network.validate;

import com.boxtrotstudio.ghost.network.sql.PlayerData;
import com.boxtrotstudio.ghost.utils.Global;
import com.boxtrotstudio.ghost.utils.WebUtils;

import java.io.IOException;

import static com.boxtrotstudio.ghost.utils.Constants.api;

public class LoginServerValidator implements Validator {
    @Override
    public PlayerData validate(String session) {
        try {
            String json = WebUtils.readContentsToString(api("info?token=" + session));

            PlayerData data =  Global.GSON.fromJson(json, PlayerData.class);
            data.normalizeStream();
            return data;
        } catch (IOException e) {
        }
        return null;
    }
}
