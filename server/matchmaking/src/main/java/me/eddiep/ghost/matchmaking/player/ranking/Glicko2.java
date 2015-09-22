package me.eddiep.ghost.matchmaking.player.ranking;

import me.eddiep.jconfig.JConfig;

import java.io.File;

public class Glicko2 {
    private static final File CONFIG_FILE = new File("ranking.conf");

    private double tau;
    private int default_rating;
    private int default_rd;
    private double default_vol;
    private Glicko2Config config;
    private long lastUpdate;
    private int updateMax;
    private int updateTime;
    private Glicko2() { }

    private static Glicko2 INSTANCE;
    public static Glicko2 getInstance() {
        if (INSTANCE != null)
            return INSTANCE;

        INSTANCE = new Glicko2();
        INSTANCE.config = JConfig.newConfigObject(Glicko2Config.class);

        if (!CONFIG_FILE.exists())
            INSTANCE.config.save(CONFIG_FILE);
        else
            INSTANCE.config.load(CONFIG_FILE);

        INSTANCE.tau = INSTANCE.config.getTau();
        INSTANCE.default_rating = INSTANCE.config.getDefaultRating();
        INSTANCE.default_rd = INSTANCE.config.getDefaultRatingDeviation();
        INSTANCE.default_vol = INSTANCE.config.getDefaultVolatility();
        INSTANCE.updateMax = INSTANCE.config.getUpdateCap();
        INSTANCE.updateTime = INSTANCE.config.getUpdateTime();
        INSTANCE.lastUpdate = INSTANCE.config.getLastUpdateTime();
        return INSTANCE;
    }

    public void performDailyUpdate() {
        performDailyUpdate(false);
    }

    public void performDailyUpdate(boolean force) {
        if (force || updateRequired()) {
            //TODO Update
        }
    }

    public boolean updateRequired() {
        return System.currentTimeMillis() - lastUpdate >= updateTime;
    }

    public me.eddiep.ghost.matchmaking.player.ranking.Rank defaultRank() {
        return new me.eddiep.ghost.matchmaking.player.ranking.Rank(default_rating, default_rd, default_vol);
    }

    public double getTau() {
        return tau;
    }

    public double getDefaultRating() {
        return default_rating;
    }
}
