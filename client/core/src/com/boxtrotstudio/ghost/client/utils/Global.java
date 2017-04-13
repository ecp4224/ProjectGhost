package com.boxtrotstudio.ghost.client.utils;

import com.google.gson.Gson;
import okhttp3.JavaNetCookieJar;
import okhttp3.OkHttpClient;

import java.net.CookieManager;
import java.net.CookiePolicy;
import java.util.Random;

public class Global {
    public static final Gson GSON = new Gson();
    public static final Random RANDOM = new Random();
    public static final CookieManager COOKIE_MANAGER = new CookieManager();
    public static final OkHttpClient HTTP = new OkHttpClient.Builder()
            .cookieJar(new JavaNetCookieJar(COOKIE_MANAGER))
            .build();

    static {
        COOKIE_MANAGER.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
    }

    public static int rand(int min, int max) {
        return RANDOM.nextInt(max - min) + min;
    }
}
