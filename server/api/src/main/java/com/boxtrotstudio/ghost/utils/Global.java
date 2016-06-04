package com.boxtrotstudio.ghost.utils;

import com.boxtrotstudio.ghost.game.match.world.timeline.Timeline;
import com.boxtrotstudio.ghost.game.match.world.timeline.TimelineSerializer;
import com.boxtrotstudio.ghost.network.Server;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.boxtrotstudio.ghost.network.sql.SQL;
import com.boxtrotstudio.ghost.network.sql.impl.OfflineDB;

import java.util.Random;

public class Global {
    public static final Random RANDOM = new Random();
    public static final long QUEUE_MS_DELAY = 2 * 1000; //10 seconds
    public static final Gson GSON = new GsonBuilder().registerTypeAdapter(Timeline.class, new TimelineSerializer()).create();
    public static SQL SQL;
    public static Server DEFAULT_SERVER;
    public static String[] ARGS;

    public static boolean isOffline() {
        return SQL instanceof OfflineDB;
    }

    public static int random(int min, int max) {
        return RANDOM.nextInt(max - min) + min;
    }

    public static float random(float min, float max) {
        return RANDOM.nextFloat() * (max - min) + min;
    }

    /**
     * Clips the specified value to the specified [min, max] interval.
     */
    public static float clip(float value, float min, float max) {
        if (value > max) {
            return max;
        } else if (value < min) {
            return min;
        }

        return value;
    }

    /**
     * Remaps a number from the range [iLow, iHigh] to the range [fLow, fHigh]
     */
    public static float map(float value, float iLow, float iHigh, float fLow, float fHigh) {
        return ((value - iLow) / (iHigh - iLow)) * (fHigh - fLow) + fLow;
    }
}
