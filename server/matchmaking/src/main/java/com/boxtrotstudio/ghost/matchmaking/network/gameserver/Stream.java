package com.boxtrotstudio.ghost.matchmaking.network.gameserver;

public enum Stream {
    /**
     * This stream is used to test updates as they're made. This means only developers can access
     * these servers
     */
    TEST(0),

    /**
     * This stream is used to indicate a server has entered the testing phase. Only the inner-team of testers
     * can access these servers
     */
    ALPHA(1),

    /**
     * This stream is used to indicate a server has passed the alpha phase. Only players in the beta program
     * can access these servers.
     */
    BETA(2),

    /**
     * This stream is used to indicate a server is pending to go live. This could be for a number of reasons:
     *
     * * The server is out of date and must update before going live
     * * This server includes a patch that has not been released yet
     * * This server is experience technical problems and shouldn't be live until those problems are fixed
     */
    BUFFERED(3),

    /**
     * This stream is used to indicate a server is live.
     */
    LIVE(4),

    /**
     * This stream is the default stream used by the matchmaking server. In most cases this is {@link Stream#LIVE}
     */
    DEFAULT(5);


    private int id;
    Stream(int id) { this.id = id; }

    public int getLevel() {
        return id;
    }

    public boolean allowed(Stream level, Stream defaultStream) {
        if (level == DEFAULT)
            level = defaultStream;

        if (level == BUFFERED)
            return false;

        int highest = 4 - level.id;
        int current = 4 - id;

        return current <= highest;
    }

    public static Stream fromInt(int level) {
        return values()[level];
    }
}
