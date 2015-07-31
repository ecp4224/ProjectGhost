package me.eddiep.ghost.game.match.world.timeline;

import me.eddiep.ghost.game.match.world.World;
import me.eddiep.ghost.network.Client;

import java.io.IOException;
import java.util.ArrayList;

public class Timeline {

    private transient World world;
    private ArrayList<WorldSnapshot> timeline = new ArrayList<>();

    public Timeline(World world) {
        this.world = world;
    }

    public World getWorld() {
        return world;
    }

    public void tick() {
        timeline.add(world.takeSnapshot());
    }

    public TimelineCursor createCursor() {
        return new TimelineCursorImpl();
    }

    public class TimelineCursorImpl implements TimelineCursor {
        private long distance = -1;
        private int cursor = timeline.size() - 1;
        private boolean stuck = false;
        private TimelineCursorListener listener;

        @Override
        public WorldSnapshot get() {
            return timeline.get(cursor);
        }

        @Override
        public Timeline getTimeline() {
            return Timeline.this;
        }

        @Override
        public long reverse(long duration) {
            long current = get().getSnapshotTaken();
            long newTime = current - duration;

            int closest = cursor;
            for (int i = cursor; i > -1; i--) {
                if (Math.abs(timeline.get(i).getSnapshotTaken() - newTime) < Math.abs(timeline.get(closest).getSnapshotTaken() - newTime)) {
                    closest = i;
                }
            }

            cursor = closest;
            stuck = true;
            return current - get().getSnapshotTaken();
        }

        @Override
        public long forward(long duration) {
            long current = get().getSnapshotTaken();
            long newTime = current + duration;

            int closest = cursor;
            int size = timeline.size();
            for (int i = cursor; i < size; i++) {
                if (Math.abs( timeline.get(i).getSnapshotTaken() - newTime ) < Math.abs( timeline.get(closest).getSnapshotTaken() - newTime )) {
                    closest = i;
                }
            }

            cursor = closest;
            stuck = true;
            return get().getSnapshotTaken() - current;
        }

        @Override
        public void present() {
            stuck = false;
            cursor = timeline.size() - 1;
            distance = -1;
        }

        @Override
        public void reset() {
            stuck = false;
            cursor = timeline.size() - 1;
            distance = -1;
        }

        @Override
        public void forwardOneTick() {
            stuck = true;
            if (cursor + 1 < timeline.size())
                cursor++;
        }

        @Override
        public void backwardsOneTick() {
            stuck = true;
            if (cursor - 1 > -1)
                cursor--;
        }

        @Override
        public void setListener(TimelineCursorListener listener) {
            this.listener = listener;
        }

        @Override
        public void tick() {
            if (!stuck) {
                if (distance == -1) {
                    cursor++;
                } else {
                    if (distanceToPresent() >= distance)
                        cursor++;
                }

                if (listener != null) {
                    listener.onTick(this);
                }
            }
        }

        @Override
        public void unstuck() {
            stuck = false;
        }

        @Override
        public boolean isStuck() {
            return stuck;
        }

        @Override
        public long distanceToPresent() {
            return Math.abs(get().getSnapshotTaken() - timeline.get(timeline.size() - 1).getSnapshotTaken());
        }

        @Override
        public void setDistanceFromPresent(long duration) {
            this.distance = duration;

            if (cursor > 0) {
                cursor = timeline.size() - 1;
                reverse(duration);
                unstuck();
            } else
                cursor = 0;
        }

        @Override
        public void sendClientSnapshot(Client client) throws IOException {
            world.updateClient(client, get());
        }

        @Override
        public int position() {
            return cursor;
        }
    }
}
