package com.boxtrotstudio.ghost.game.match.world.timeline;

public interface TimelineCursor {
    WorldSnapshot get();

    /**
     * Get the timeline this cursor belongs to.
     * @return The timeline this cursor belongs to
     */
    Timeline getTimeline();

    /**
     * Reverse back in time for <b>duration</b> ms. This will keep the cursor stuck at this point in time.
     * @param duration How far back in time to go back
     * @return How far back in time was actually transversed
     */
    long reverse(long duration);

    /**
     * Move forward in time for <b>duration</b> ms. This will keep the cursor stuck at this point in time.
     * @param duration How far forward in time to go
     * @return How far forward in time was actually transversed
     */
    long forward(long duration);

    /**
     * Reset the cursor to the present. This will update the cursor to the current present every tick.
     */
    void present();

    /**
     * Reset the cursor to the first tick. This will keep the cursor stuck to the current position every tick
     */
    void reset();

    /**
     * Move the cursor forward in time by one tick, if possible. This will keep the cursor stuck at this point in time.
     */
    void forwardOneTick();

    /**
     * Move the cursor backwards in time by one tick, if possible. This will keep the cursor stuck at this point in time.
     */
    void backwardsOneTick();

    /**
     * Set the {@link TimelineCursorListener} for this cursor
     * @param listener The listener to use for this cursor
     */
    void setListener(TimelineCursorListener listener);

    /**
     * Execute a tick on this cursor
     */
    void tick();

    /**
     * Unstuck this cursor. This will automatically move the cursor one tick each game tick.
     */
    void unstuck();

    /**
     * Returns whether this cursor is moving during game ticks
     * @return True if this cursor will automatically move one tick each game tick, otherwise false
     */
    boolean isStuck();

    /**
     * Returns how far this cursor is from the present, in ms.
     * @return How far this cursor is from the present
     */
    long distanceToPresent();

    /**
     * Set how far away this cursor should stay away from the present, in ms. For example, if you want this cursor
     * to lag behind 3 seconds, you can use this method and set the duration to 3000
     * @param duration How far away to stay back from the present, in ms.
     */
    void setDistanceFromPresent(long duration);

    /**
     * Get the position this cursor is currently on
     * @return The position in ticks
     */
    int position();

    /**
     * Whether this cursor is in the present
     * @return True if this cursor is in the preset, otherwise false
     */
    boolean isPresent();

    void setPosition(int position);
}
