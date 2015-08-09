package me.eddiep.ghost.utils;

import com.google.gson.Gson;
import me.eddiep.ghost.network.sql.SQL;

import java.util.Random;

public class Global {
    public static final Random RANDOM = new Random();
    public static final long QUEUE_MS_DELAY = 10 * 1000; //10 seconds
    public static final Gson GSON = new Gson();
    public static SQL SQL;

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
