package com.boxtrotstudio.ghost.game.match.stats;

import org.bson.Document;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

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
            stats.put(stat, 1L);

        stats.put(stat, stats.get(stat) + 1);
        return this;
    }

    public TemporaryStats subtractOne(String stat) {
        if (!stats.containsKey(stat))
            stats.put(stat, 0L);

        stats.put(stat, stats.get(stat) - 1);
        return this;
    }

    public Document asDocument() {
        Document document = new Document();

        String[] keys = new String[stats.keySet().size()];
        int i = 0;
        for (String key : stats.keySet()) {
            keys[i] = key;
            document.append(key, stats.get(key));
            i++;
        }

        document.append("stats", Arrays.asList(keys));
        
        return document;
    }
    
    public static TemporaryStats fromDocument(Document document) {
        TemporaryStats stats = new TemporaryStats();
        
        List<String> keys = document.get("stats", List.class);
        
        for (String key : keys) {
            long i = document.getLong(key);
            stats.stats.put(key, i);
        }
        
        return stats;
    }
}
