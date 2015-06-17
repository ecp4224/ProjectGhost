package me.eddiep.ghost.server.game.stats;

import me.eddiep.ghost.server.game.entities.PlayableEntity;
import me.eddiep.ghost.server.game.entities.playable.impl.Player;
import org.bson.Document;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import static me.eddiep.ghost.server.utils.Constants.SECONDS_PER_TICK;

public class TrackingMatchStats {
    private long lastShotsFired;
    private long lastShotsHit;
    private int ticksVisible;
    private int ticksInvisible;
    private int lastHatTrick;
    private PlayableEntity player;
    private boolean finalized;
    private LinkedList<StatsInSecond> timeline = new LinkedList<>();
    private FinalizedMatchStats stats = null;

    public TrackingMatchStats(PlayableEntity p) {
        if (!p.isInMatch())
            throw new IllegalStateException("This playable is not in a match!");

        this.player = p;

        //lastShotsFired = playable.getTotalShotsFired();
        //lastShotsHit = playable.getShotsHit();
        ticksVisible = 0;
        ticksInvisible = 0;
        //lastHatTrick = playable.getHatTrickCount();
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
            /*StatsInSecond stat = new StatsInSecond((int)(playable.getTotalShotsFired() - lastShotsFired),
                    (int)(playable.getShotsHit() - lastShotsHit),
                    ticksVisible, ticksInvisible,
                    playable.getLives(),
                    playable.getHatTrickCount() - lastHatTrick,
                    playable.getEntity().getX(),
                    playable.getEntity().getY(),
                    timeline.size());*/

            StatsInSecond stat = new StatsInSecond(
                    player.getCurrentMatchStats(),
                    ticksVisible, ticksInvisible,
                    player.getLives(),
                    player.getX(),
                    player.getY(),
                    timeline.size());

            timeline.add(stat);

            tickCount = 0;
        }
    }

    public static class StatsInSecond {
        private TemporaryStats stats;
        private double secondsVisible;
        private double secondsInvisible;
        private int lives;
        private float x;
        private float y;
        private int second;

        public StatsInSecond(TemporaryStats stats, int ticksVisible, int ticksInvisible, int lives, float x, float y, int second) {
            this.stats = stats;

            this.secondsVisible = (SECONDS_PER_TICK * ticksVisible);
            this.secondsInvisible = (SECONDS_PER_TICK * ticksInvisible);
            this.lives = lives;
            this.x = x;
            this.y = y;
            this.second = second;
        }

        public StatsInSecond(TemporaryStats stats, double secondsVisible, double secondsInvisible, int lives, float x, float y, int second) {
            this.stats = stats;

            this.secondsVisible = secondsVisible;
            this.secondsInvisible = secondsInvisible;
            this.lives = lives;
            this.second = second;
        }

        public TemporaryStats getStats() {
            return stats;
        }

        public double getSecondsVisible() {
            return secondsVisible;
        }

        public double getSecondsInvisible() {
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

        public int getSecond() {
            return second;
        }

        public Document asDocument() {
            return new Document("stats", stats.asDocument())
                    .append("secondsVisible", secondsVisible)
                    .append("secondsInvisible", secondsInvisible)
                    .append("lives", lives)
                    .append("second", second)
                    .append("x", x)
                    .append("y", y);
        }

        public static StatsInSecond fromDocument(Document d) {
            double secondsVisible = d.getDouble("secondsVisible");
            double secondsInvisible = d.getDouble("secondsInvisible");
            int lives = d.getInteger("lives");
            int second = d.getInteger("second");
            float x = d.getDouble("x").floatValue();
            float y = d.getDouble("y").floatValue();
            TemporaryStats stats = TemporaryStats.fromDocument(d.get("stats", Document.class));

            return new StatsInSecond(stats, secondsVisible, secondsInvisible, lives, x, y, second);
            //return new StatsInSecond(accuracy, secondsVisible, secondsInvisible, lives, hats, x, y, second);
        }
    }

    public static class FinalizedMatchStats {
        private long playerId;
        private StatsInSecond[] timeline;

        private FinalizedMatchStats(TrackingMatchStats stats) {
            if (stats.player instanceof Player)
                this.playerId = ((Player)stats.player).getPlayerID();
            else
                this.playerId = 0;
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
