package me.eddiep.ghost.client.core.game;

import java.util.HashMap;

public class TemporaryStats {
    public static final String SHOTS_FIRED = "shots_fired";
    public static final String SHOTS_HIT = "shots_hit";
    public static final String HAT_TRICK = "hat_trick";
    public static final String WEAPON = "weapon";
    public static final String ITEM_USAGE = "item";

    private HashMap<String, Long> stats = new HashMap<>();

    public TemporaryStats() { }

    public TemporaryStats set(String stat, long value) {
        stats.put(stat, value);
        return this;
    }

    public TemporaryStats plusOne(String stat) {
        if (!stats.containsKey(stat))
            stats.put(stat, 0L);

        stats.put(stat, stats.get(stat) + 1);
        return this;
    }

    public TemporaryStats subtractOne(String stat) {
        if (!stats.containsKey(stat))
            stats.put(stat, 0L);

        stats.put(stat, stats.get(stat) - 1);
        return this;
    }

    public long get(String stat) {
        if (!stats.containsKey(stat))
            return 0;
        return stats.get(stat);
    }
}
