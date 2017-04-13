package com.boxtrotstudio.ghost.network.validate;

import com.boxtrotstudio.ghost.network.sql.PlayerData;
import com.boxtrotstudio.ghost.utils.Global;
import com.boxtrotstudio.ghost.utils.WebUtils;

import java.io.IOException;

import static com.boxtrotstudio.ghost.utils.Constants.api;

public class LoginServerValidator implements Validator {
    @Override
    public PlayerData validate(String session) {


        //PlayerData data =  Global.GSON.fromJson(json, PlayerData.class);
        //data.normalizeStream();
        //return data;
        return null;
    }
}
