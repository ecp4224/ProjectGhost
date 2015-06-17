package me.eddiep.ghost.server.game.stats;

import static me.eddiep.ghost.server.utils.Constants.*;

import me.eddiep.ghost.server.game.entities.playable.impl.Player;
import java.util.LinkedList;

public class TrackingMatchStats {
    private long lastShotsFired;
    private long lastShotsHit;
    private int ticksVisible;
    private int ticksInvisible;
    private int lastHatTrick;
    private Player player;
    private boolean finalized;
    private LinkedList<StatsInSecond> timeline = new LinkedList<>();
    private FinalizedMatchStats stats = null;

    public TrackingMatchStats(Player p) {
        if (!p.isInMatch())
            throw new IllegalStateException("This playable is not in a match!");

        this.player = p;

        lastShotsFired = player.getTotalShotsFired();
        lastShotsHit = player.getShotsHit();
        ticksVisible = 0;
        ticksInvisible = 0;
        lastHatTrick = player.getHatTrickCount();
    }

    public FinalizedMatchStats preview() {
        return new FinalizedMatchStats(this);
    }

    public FinalizedMatchStats finalized() {
        if (finalized) {
            return stats;
        }
        finalized = true;
        stats = new FinalizedMatchStats(this);
        timeline.clear();
        timeline = null;
        player = null;
        return stats;
    }

    private int tickCount;
    public void tick() {
        if (finalized)
            throw new IllegalStateException("This tracker is already done tracking!");

        tickCount++;
        if (player.isVisible())
            ticksVisible++;
        else
            ticksInvisible++;

        if (tickCount >= 60) {
            StatsInSecond stat = new StatsInSecond((int)(player.getTotalShotsFired() - lastShotsFired),
                    (int)(player.getShotsHit() - lastShotsHit),
                    ticksVisible, ticksInvisible,
                    player.getLives(),
                    player.getHatTrickCount() - lastHatTrick,
                    player.getX(),
                    player.getY(),
                    timeline.size());

            timeline.add(stat);

            tickCount = 0;
        }
    }

    public static class StatsInSecond {
        private double accuracy;
        private int secondsVisible;
        private int secondsInvisible;
        private int lives;
        private int hatTrickCount;
        private float x;
        private float y;
        private int second;

        public StatsInSecond(int shotsFired, int shotsHit, int ticksVisible, int ticksInvisible, int lives, int hatTrickCount, float x, float y, int second) {
            if (shotsFired == 0)
                this.accuracy = 0.0;
            else {
                this.accuracy = (double) shotsHit / (double) shotsFired;
                this.accuracy *= 100.0;
            }

            this.secondsVisible = (int) (SECONDS_PER_TICK * ticksVisible);
            this.secondsInvisible = (int) (SECONDS_PER_TICK * ticksInvisible);
            this.lives = lives;
            this.x = x;
            this.y = y;
            this.hatTrickCount = hatTrickCount;
            this.second = second;
        }

        public StatsInSecond(double accuracy, int secondsVisible, int secondsInvisible, int lives, int hatTrickCount, float x, float y, int second) {
            this.accuracy = accuracy;
            this.secondsVisible = secondsVisible;
            this.secondsInvisible = secondsInvisible;
            this.lives = lives;
            this.hatTrickCount = hatTrickCount;
            this.second = second;
        }

        public double getAccuracy() {
            return accuracy;
        }

        public int getSecondsVisible() {
            return secondsVisible;
        }

        public int getSecondsInvisible() {
            return secondsInvisible;
        }

        public int getLives() {
            return lives;
        }

        public float getX() {
            return x;
        }

        public float getY() {
            return y;
        }

        public int getHatTrickCount() {
            return hatTrickCount;
        }

        public int getSecond() {
            return second;
        }
    }

    public static class FinalizedMatchStats {
        private long playerId;
        private StatsInSecond[] timeline;

        private FinalizedMatchStats(TrackingMatchStats stats) {
            this.playerId = stats.player.getPlayerID();
            this.timeline = stats.timeline.toArray(new StatsInSecond[stats.timeline.size()]);
        }

        private FinalizedMatchStats() { }

        public StatsInSecond[] getTimeline() {
            return timeline;
        }

        public long getPlayerId() {
            return playerId;
        }
    }
}
