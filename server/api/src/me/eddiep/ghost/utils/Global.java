package me.eddiep.ghost.utils;

import me.eddiep.ghost.network.sql.SQL;

import java.util.Random;

public class Global {
    public static final Random RANDOM = new Random();
    public static final long QUEUE_MS_DELAY = 10 * 1000; //10 seconds

    public static SQL SQL;

    public static int random(int min, int max) {
        return RANDOM.nextInt(max - min) + min;
    }

    public static float random(float min, float max) {
        return RANDOM.nextFloat() * (max - min) + min;
    }
}
