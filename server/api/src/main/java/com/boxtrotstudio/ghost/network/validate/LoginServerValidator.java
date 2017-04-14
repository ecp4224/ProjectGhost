package com.boxtrotstudio.ghost.network.validate;

import com.boxtrotstudio.ghost.network.sql.PlayerData;
import com.boxtrotstudio.ghost.utils.Global;
import com.boxtrotstudio.ghost.utils.WebUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

import static com.boxtrotstudio.ghost.utils.Constants.api;

public class LoginServerValidator implements Validator {
    @Override
    public PlayerData validate(String session) {
        CookieStore cookieStore = new BasicCookieStore();
        CloseableHttpClient client = HttpClientBuilder.create()
                .setDefaultCookieStore(cookieStore)
                .build();

        String[] cookies = session.split(";");
        for (String cookie : cookies) {
            String[] keyvalue = cookie.split("=");
            BasicClientCookie clientCookie = new BasicClientCookie(keyvalue[0], keyvalue[1]);
            clientCookie.setPath("/");
            clientCookie.setDomain("stage.projecthost.io");

            cookieStore.addCookie(clientCookie);
        }

        try {
            HttpEntity entity = client.execute(new HttpGet(api("info"))).getEntity();
            String json = EntityUtils.toString(entity, "UTF-8");

            PlayerData data =  Global.GSON.fromJson(json, PlayerData.class);
            data.normalizeStream();
            return data;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
