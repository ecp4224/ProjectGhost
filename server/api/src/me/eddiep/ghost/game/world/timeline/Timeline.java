package me.eddiep.ghost.game.world.timeline;

import me.eddiep.ghost.game.world.World;

import java.util.ArrayList;

public class Timeline {

    private World world;
    private ArrayList<WorldSnapshot> timeline = new ArrayList<>();
    private int cursor;
    private boolean stuck = false;

    public Timeline(World world) {
        this.world = world;
    }

    public World getWorld() {
        return world;
    }

    public void tick() {
        timeline.add(world.takeSnapshot());
        if (!stuck) {
            cursor = timeline.size() - 1;
        }
    }

    public WorldSnapshot get() {
        return timeline.get(cursor);
    }

    /**
     * Reverse back in time for <b>duration</b> ms. This will keep the cursor stuck at this point in time.
     * @param duration How far back in time to go back
     * @return How far back in time was actually transversed
     */
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

    /**
     * Move forward in time for <b>duration</b> ms. This will keep the cursor stuck at this point in time.
     * @param duration How far forward in time to go
     * @return How far forward in time was actually transversed
     */
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

    /**
     * Reset the cursor to the present. This will update the cursor to the current present every tick.
     */
    public void present() {
        stuck = false;
        cursor = timeline.size() - 1;
    }

    /**
     * Reset the cursor to the present. This will update the cursor to the current present every tick.
     * @see Timeline#present()
     */
    public void reset() {
        stuck = false;
        cursor = timeline.size() - 1;
    }

    /**
     * Move the cursor forward in time by one tick, if possible. This will keep the cursor stuck at this point in time.
     */
    public void forwardOneTick() {
        stuck = true;
        if (cursor + 1 < timeline.size())
            cursor++;
    }

    /**
     * Move the cursor backwards in time by one tick, if possible. This will keep the cursor stuck at this point in time.
     */
    public void backwardsOneTick() {
        stuck = true;
        if (cursor - 1 > -1)
            cursor--;
    }
}
