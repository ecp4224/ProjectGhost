package me.eddiep.ghost.client.utils;

import com.google.gson.Gson;

import java.util.Random;

public class Global {
    public static final Gson GSON = new Gson();
    public static final Random RANDOM = new Random();

    public static int rand(int min, int max) {
        return RANDOM.nextInt(max - min) + min;
    }
}
