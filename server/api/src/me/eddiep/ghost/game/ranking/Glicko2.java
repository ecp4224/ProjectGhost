package me.eddiep.ghost.game.ranking;

import me.eddiep.ghost.game.LiveMatch;
import me.eddiep.ghost.utils.Global;
import me.eddiep.ghost.game.entities.playable.impl.BaseNetworkPlayer;
import me.eddiep.ghost.network.sql.PlayerData;
import me.eddiep.ghost.network.sql.PlayerUpdate;
import me.eddiep.ghost.network.sql.SQL;
import me.eddiep.jconfig.JConfig;

import java.io.File;
import java.util.List;

public class Glicko2 {
    private static final File CONFIG_FILE = new File("ranking.conf");

    private double tau;
    private int default_rating;
    private int default_rd;
    private double default_vol;
    private String algorithm;
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
        INSTANCE.algorithm = INSTANCE.config.getVolatilityAlgorithm();
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
            SQL sql = Global.SQL;
            long totalPlayers = sql.getPlayerCount();
            System.err.println("[SERVER] Updating " + totalPlayers + " players ranks...");

            long i = 0;
            while (true) {
                List<PlayerData> datas = sql.fetchPlayerStats(i, i + updateMax);
                if (datas.size() == 0)
                    break;

                PlayerUpdate[] updates = new PlayerUpdate[datas.size()];

                for (int j = 0; j < updates.length; j++) {
                    PlayerData p = datas.get(j);
                    Rank r = p.getRank();
                    r.update();

                    PlayerUpdate update = new PlayerUpdate(p);
                    update.updateRank(r);

                    updates[j] = update;
                }

                sql.bulkUpdate(updates);

                i += datas.size() + 1;

                if (datas.size() < updateMax)
                    break;
            }
            System.err.println("[SERVER] Finished updating " + totalPlayers + " players ranks!");

            lastUpdate = System.currentTimeMillis();

            config.setLastUpdateTime(lastUpdate);
            config.save(CONFIG_FILE);
        }
    }

    public boolean updateRequired() {
        return System.currentTimeMillis() - lastUpdate >= updateTime;
    }

    public Rank defaultRank() {
        return new Rank(default_rating, default_rd, default_vol);
    }

    public double getTau() {
        return tau;
    }

    public double getDefaultRating() {
        return default_rating;
    }

    public String getAlgorithm() {
        return algorithm;
    }

    /*
            when outcome is 1:
                player1 is winner
                player2 is loser

            when outcome is 0:
                player1 is loser
                player2 is winner

            when outcome is 0.5:
                player1 is winner
                player2 is winner

            player1.addResult(player2, outcome);
            player2.addResult(player1, 1 - outcome);
     */
    public void completeMatch(LiveMatch match) {
        if (match.getWinningTeam() == null || match.getLosingTeam() == null) {
            for (BaseNetworkPlayer winner : match.getTeam1().getPlayers()) {
                for (BaseNetworkPlayer loser : match.getTeam2().getPlayers()) {
                    winner.getRanking().addResult(loser, 0.5);
                }
            }

            for (BaseNetworkPlayer loser : match.getTeam2().getPlayers()) {
                for (BaseNetworkPlayer winners : match.getTeam1().getPlayers()) {
                    loser.getRanking().addResult(winners, 0.5);
                }
            }
        } else {
            for (BaseNetworkPlayer winner : match.getWinningTeam().getPlayers()) {
                for (BaseNetworkPlayer loser : match.getLosingTeam().getPlayers()) {
                    winner.getRanking().addResult(loser, 1);
                }
            }

            for (BaseNetworkPlayer loser : match.getLosingTeam().getPlayers()) {
                for (BaseNetworkPlayer winners : match.getWinningTeam().getPlayers()) {
                    loser.getRanking().addResult(winners, 0);
                }
            }
        }
    }
}