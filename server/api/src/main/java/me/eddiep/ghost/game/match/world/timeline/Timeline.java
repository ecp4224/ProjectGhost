package me.eddiep.ghost.game.match.world.timeline;

import me.eddiep.ghost.game.match.world.World;

import java.util.ArrayList;
import java.util.Iterator;

import static me.eddiep.ghost.utils.Constants.AVERAGE_MATCH_TIME;

public class Timeline implements Iterable<WorldSnapshot> {

    private transient World world;
    //private ArrayList<WorldSnapshot> timeline = new ArrayList<>((int) (AVERAGE_MATCH_TIME * 16));
    private SnapshotNode timelineBeginning;
    private SnapshotNode timelineLast;
    private int timelineSize;

    public Timeline(World world) {
        this.world = world;
    }

    public World getWorld() {
        return world;
    }

    public void tick() {
        WorldSnapshot tick = world.takeSnapshot();
        if (timelineBeginning == null) {
            SnapshotNode beginning = new SnapshotNode(tick);
            timelineBeginning = beginning;
            timelineLast = beginning;
        } else {
            SnapshotNode newLast = new SnapshotNode(tick);

            newLast.setPrevious(timelineLast);
            timelineLast.setNext(newLast);

            timelineLast = newLast;
        }
        timelineSize++;
        //timeline.add(world.takeSnapshot());
    }

    public TimelineCursor createCursor() {
        return new TimelineCursorImpl();
    }

    public void dispose() {
        timelineBeginning = null;
        timelineLast = null;
        //timeline.clear();
        //timeline = null;
        world = null;
    }

    public int size() {
        return timelineSize;
    }

    @Override
    public Iterator<WorldSnapshot> iterator() {
        return new TimelineCursorImpl();
    }

    public class TimelineCursorImpl implements TimelineCursor, Iterator<WorldSnapshot> {
        private long distance = -1;
        private int cursor = timelineSize - 1;
        private SnapshotNode currentNode;
        private boolean stuck = false;
        private TimelineCursorListener listener;

        public TimelineCursorImpl() {
            currentNode = timelineBeginning;
        }

        @Override
        public WorldSnapshot get() {
            if (currentNode == null) {
                reset();
                unstuck();
            }
            return currentNode.getData();
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
            SnapshotNode closestNode = currentNode;
            SnapshotNode ci = currentNode;
            for (int i = cursor; i > -1; i--) {
                if (Math.abs(ci.getData().getSnapshotTaken() - newTime) < Math.abs(closestNode.getData().getSnapshotTaken() - newTime)) {
                    closest = i;
                    closestNode = ci;
                }

                if (ci.getPrevious() == null)
                    break;
                ci = ci.getPrevious();
            }

            cursor = closest;
            currentNode = closestNode;
            stuck = true;
            return current - get().getSnapshotTaken();
        }

        @Override
        public long forward(long duration) {
            long current = get().getSnapshotTaken();
            long newTime = current + duration;

            int closest = cursor;
            SnapshotNode closestNode = currentNode;
            SnapshotNode ci = currentNode;

            int size = timelineSize;
            long closestValue = -1;
            for (int i = cursor; i < size; i++) {
                WorldSnapshot snap = ci.getData();
                long dis = Math.abs( snap.getSnapshotTaken() - newTime );

                if (dis < closestValue || closestValue == -1) {
                    closest = i;
                    closestValue = dis;
                }

                if (ci.getNext() == null)
                    break;
                ci = ci.getNext();
            }

            cursor = closest;
            currentNode = closestNode;
            stuck = true;
            return get().getSnapshotTaken() - current;
        }

        @Override
        public void present() {
            stuck = false;
            cursor = timelineSize - 1;
            currentNode = timelineLast;
            distance = -1;
        }

        @Override
        public void reset() {
            stuck = true;
            cursor = 0;
            currentNode = timelineBeginning;
            distance = -1;
        }

        @Override
        public void forwardOneTick() {
            stuck = true;
            if (cursor + 1 < timelineSize) {
                cursor++;
                currentNode = currentNode.getNext();
            }
        }

        @Override
        public void backwardsOneTick() {
            stuck = true;
            if (cursor - 1 > -1) {
                cursor--;
                currentNode = currentNode.getPrevious();
            }
        }

        @Override
        public void setListener(TimelineCursorListener listener) {
            this.listener = listener;
        }

        @Override
        public void tick() {
            if (currentNode == null) {
                reset();
                unstuck();
            }

            if (!stuck) {
                if (distance == -1) {
                    cursor++;
                    currentNode = currentNode.getNext();
                } else {
                    if (distanceToPresent() >= distance) {
                        cursor++;
                        currentNode = currentNode.getNext();
                    }
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
            return Math.abs(get().getSnapshotTaken() - timelineLast.getData().getSnapshotTaken());
        }

        @Override
        public void setDistanceFromPresent(long duration) {
            this.distance = duration;

            if (cursor > 0) {
                cursor = timelineSize - 1;
                reverse(duration);
                unstuck();
            } else
                cursor = 0;
        }

        @Override
        public int position() {
            return cursor;
        }

        @Override
        public boolean isPresent() {
            return cursor + 1 >= timelineSize;
        }

        @Override
        public void setPosition(int position) {
            cursor = position;
            currentNode = timelineBeginning;
            for (int i = 0; i < position; i++) {
                currentNode = currentNode.getNext();
            }
            stuck = true;
        }

        @Override
        public boolean hasNext() {
            return !isPresent();
        }

        @Override
        public WorldSnapshot next() {
            return get();
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("This iterator cannot remove!");
        }
    }

    private class SnapshotNode {
        private SnapshotNode next, previous;
        private WorldSnapshot data;

        public SnapshotNode(WorldSnapshot data) {
            this.data = data;
        }

        public SnapshotNode getNext() {
            return next;
        }

        public void setNext(SnapshotNode next) {
            this.next = next;
        }

        public SnapshotNode getPrevious() {
            return previous;
        }

        public void setPrevious(SnapshotNode previous) {
            this.previous = previous;
        }

        public WorldSnapshot getData() {
            return data;
        }

        public void setData(WorldSnapshot data) {
            this.data = data;
        }
    }
}
