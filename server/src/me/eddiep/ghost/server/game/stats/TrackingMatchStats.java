package me.eddiep.ghost.server.game.stats;

import static me.eddiep.ghost.server.utils.Constants.*;

import me.eddiep.ghost.server.game.ActiveMatch;
import me.eddiep.ghost.server.game.entities.Player;
import org.bson.Document;

import javax.print.Doc;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

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
            throw new IllegalStateException("This player is not in a match!");

        this.player = p;

        lastShotsFired = player.getTotalShotsFired();
        lastShotsHit = player.getShotsHit();
        ticksVisible = 0;
        ticksInvisible = 0;
        lastHatTrick = player.getHatTrickCount();
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

        public Document asDocument() {
            return new Document("accuracy", accuracy)
                    .append("secondsVisible", secondsVisible)
                    .append("secondsInvisible", secondsInvisible)
                    .append("lives", lives)
                    .append("hatTrickCount", hatTrickCount)
                    .append("second", second)
                    .append("x", x)
                    .append("y", y);
        }

        public static StatsInSecond fromDocument(Document d) {
            double accuracy = d.getDouble("accuracy");
            int secondsVisible = d.getInteger("secondsVisible");
            int secondsInvisible = d.getInteger("secondsInvisible");
            int lives = d.getInteger("lives");
            int hats = d.getInteger("hatTrickCount");
            int second = d.getInteger("second");
            float x = d.getDouble("x").floatValue();
            float y = d.getDouble("y").floatValue();

            return new StatsInSecond(accuracy, secondsVisible, secondsInvisible, lives, hats, x, y, second);
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

        public Document asDocument() {
            Document[] documents = new Document[timeline.length];

            for (int i = 0; i < documents.length; i++) {
                documents[i] = timeline[i].asDocument();
            }

            return new Document("playerId", playerId)
                    .append("timeline", Arrays.asList(documents));
        }

        public static FinalizedMatchStats fromDocument(Document document) {
            FinalizedMatchStats stats = new FinalizedMatchStats();

            stats.playerId = document.getLong("playerId");

            List<Document> documentList = document.get("timeline", List.class);

            StatsInSecond[] seconds = new StatsInSecond[documentList.size()];

            for (int i = 0; i < seconds.length; i++) {
                seconds[i] = StatsInSecond.fromDocument(documentList.get(i));
            }

            stats.timeline = seconds;

            return stats;
        }
    }
}
