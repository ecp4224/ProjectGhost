package com.boxtrotstudio.ghost.utils;

import com.boxtrotstudio.ghost.game.match.entities.PlayableEntity;

import java.net.MalformedURLException;
import java.net.URL;

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

    /**
     * The amount of ranked games a player must play before getting ranked
     */
    public static final int PLACEMENT_GAME_COUNT = 15;

    /**
     * The amount of ranked games a player must play before calculating a new rank
     */
    public static final int RANKED_GAME_COUNT = 10;

    //=== RANKING CONSTANTS ===
    public static final double SCALING_FACTOR = 173.7378;
    //=== RANKING CONSTANTS ===

    //=== LOGIN SERVER CONSTANTS ===
    /**
     * The domain of the login server API
     */
    public static final String WEB_API_DOMAIN = "https://api.boxtrotstudio.com/";

    /**
     * The version of the login server API to use
     */
    public static final String API_VERSION = "v1";

    //=== LOGIN SERVER CONSTANTS ===

    //=== SERVER CONSTANTS ===

    /**
     * The version of this server
     */
    public static final int VERSION = 1;

    //=== SERVER CONSTANTS ===

    //=== GAME CONSTANTS ===

    public static final int COUNTDOWN_LIMIT = 5;

    public static final long AVERAGE_MATCH_TIME = 60_000;

    public static final long READY_TIMEOUT = 30000;

    /**
     * The range around the flag that team members will spawn in
     */
    public static final int FLAG_RESPAWN_RANGE = 250;

    /**
     * How often we send the {@link me.eddiep.ghost.server.network.packet.impl.EntityStatePacket} to all clients
     */
    public static final long UPDATE_STATE_INTERVAL = 50;

    /**
     * How long it takes for a {@link PlayableEntity} to fade in/out
     */
    public static final long FADE_SPEED = 700;

    /**
     * How many packets will be sent while an entity is invisible
     * @deprecated This method is no longer used
     */
    public static final long MAX_INVISIBLE_PACKET_COUNT = FADE_SPEED / (1000L / UPDATE_STATE_INTERVAL);


    public static final int TICKS_PER_SECONDS = 60;

    /**
     * How many seconds occurs in 1 tick
     */
    public static final double SECONDS_PER_TICK = 1.0 / TICKS_PER_SECONDS;

    /**
     * How many milliseconds occurs in 1 tick
     */
    public static final double MS_PER_TICK = SECONDS_PER_TICK * 1000.0;

    /**
     * How fast the indicator will increase each <b>tick</b>
     */
    public static final int VISIBLE_COUNTER_INCREASE_RATE = 3; //Increase 3 steps every tick

    /**
     * How fast the indicator will decrease each <b>tick</b>
     */
    public static final int VISIBLE_COUNTER_DECREASE_RATE = 6; //Decrease 6 steps every tick

    /**
     * What the indicator's value must be for the playable to start fading in
     */
    public static final int VISIBLE_COUNTER_START_FADE = 360 * VISIBLE_COUNTER_INCREASE_RATE; //360 ticks or 6 seconds

    /**
     * What the indicator's value must be for the playable to be fully visible
     */
    public static final int VISIBLE_COUNTER_FULLY_VISIBLE = 402 * VISIBLE_COUNTER_INCREASE_RATE; //402 ticks or 6.7 seconds

    /**
     * The distance between the two constants above
     */
    public static final int VISIBLE_COUNTER_FADE_DISTANCE = VISIBLE_COUNTER_FULLY_VISIBLE - VISIBLE_COUNTER_START_FADE;

    /**
     * The default value the indicator will be set to if the playable shoots/gets hit before they are fully visible
     */
    public static final int VISIBLE_COUNTER_DEFAULT_LENGTH = (1000 * TICKS_PER_SECONDS) + VISIBLE_COUNTER_FULLY_VISIBLE; //1 second past fully visible time
    public static final String SLACK_WEBHOOK_URL = "https://hooks.slack.com/services/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";

    public static URL api(String endPoint) {
        try {
            WebUtils.trustLetsEncrypt();
            return new URL(WEB_API_DOMAIN + API_VERSION + "/" + endPoint);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
