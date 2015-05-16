package me.eddiep.ghost.server.utils;

public class Constants {
    //=== SQL FIELD NAMES ===
    public static final String HAT_TRICK = "hatTricks";
    public static final String PLAYERS_KILLED = "playersKilled";
    public static final String USERNAME = "username";
    public static final String DISPLAY_NAME = "displayName";
    public static final String ID = "id";
    public static final String SHOTS_HIT = "shotsHit";
    public static final String SHOTS_MISSED = "shotsMissed";
    public static final String WINS = "wins";
    public static final String LOSES = "loses";
    public static final String HASH = "hash";
    public static final String RANK = "rank";
    public static final String FRIENDS = "friends";
    //=== SQL FIELD NAMES ===

    //=== RANKING CONSTANTS ===
    public static final double SCALING_FACTOR = 173.7378;
    //=== RANKING CONSTANTS ===

    //=== GAME CONSTANTS ===
    public static final int TICKS_PER_SECONDS = 60;
    public static final double SECONDS_PER_TICK = 1.0 / TICKS_PER_SECONDS;
    public static final double MS_PER_TICK = SECONDS_PER_TICK * 1000.0;

    public static final int VISIBLE_COUNTER_INCREASE_RATE = 3; //Increase 3 steps every tick
    public static final int VISIBLE_COUNTER_DECREASE_RATE = 6; //Decrease 6 steps every tick
    public static final int VISIBLE_COUNTER_START_FADE = 360 * VISIBLE_COUNTER_INCREASE_RATE; //360 ticks or 6 seconds
    public static final int VISIBLE_COUNTER_FULLY_VISIBLE = 402 * VISIBLE_COUNTER_INCREASE_RATE; //444 ticks or 6.7 seconds
    public static final int VISIBLE_COUNTER_FADE_DISTANCE = VISIBLE_COUNTER_FULLY_VISIBLE - VISIBLE_COUNTER_START_FADE;
    public static final int VISIBLE_COUNTER_DEFAULT_LENGTH = (1000 * TICKS_PER_SECONDS) + VISIBLE_COUNTER_FULLY_VISIBLE; //1 second past fully visible time
}
