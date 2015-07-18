package me.eddiep.ghost.game.match.world;

import me.eddiep.ghost.game.match.entities.Entity;
import me.eddiep.ghost.game.match.LiveMatch;
import me.eddiep.ghost.game.match.entities.PlayableEntity;
import me.eddiep.ghost.game.match.world.timeline.*;
import me.eddiep.ghost.network.Server;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static me.eddiep.ghost.utils.Constants.UPDATE_STATE_INTERVAL;

public abstract class WorldImpl implements World {
    protected ArrayList<Entity> entities = new ArrayList<>();
    private ArrayList<Entity> toAdd = new ArrayList<>();
    private ArrayList<Entity> toRemove = new ArrayList<>();

    private ArrayList<EntitySpawnSnapshot> spawns = new ArrayList<>();
    private ArrayList<EntityDespawnSnapshot> despawns = new ArrayList<>();
    private ArrayList<PlayableSnapshot> playableChanges = new ArrayList<>();
    protected Timeline timeline;
    protected LiveMatch match;
    private AtomicBoolean isTicking = new AtomicBoolean(false);
    protected long lastEntityUpdate;
    protected Server server;

    private boolean active, idle, disposed;

    public WorldImpl(LiveMatch match) {
        this.match = match;
        this.timeline = new Timeline(this);
        this.server = match.getServer();
    }

    @Override
    public abstract void onLoad();

    @Override
    public void spawnEntity(Entity entity) {
        if (isTicking.get()) {
            toAdd.add(entity);
        } else {
            entities.add(entity);
        }

        spawns.add(EntitySpawnSnapshot.createEvent(entity));

        entity.setWorld(this);
    }

    @Override
    public void despawnEntity(Entity entity) {
        if (isTicking.get()) {
            toRemove.add(entity);
        } else {
            entities.remove(entity);
        }

        despawns.add(EntityDespawnSnapshot.createEvent(entity));

        entity.setWorld(null);
    }

    @Override
    public boolean isInWorld(Entity entity) {
        return entities.contains(entity);
    }

    @Override
    public void tick() {
        if (disposed) {
            System.err.println("[SERVER] Ticked invoked on disposed world! Ignoring..");
            return;
        }

        if (active) {
            activeTick();
        } else if (idle) {
            idleTick();
        }

        if (shouldRequestTick()) {
            server.executeNextTick(new Runnable() {
                @Override
                public void run() {
                    tick();
                }
            });
        } else { //This world no longer wants ticks so it's not needed
            dispose();
        }
    }

    @Override
    public void dispose() {
        if (disposed)
            return;

        disposed = true;

        System.out.println("[SERVER] Disposing world for match " + match.getID());

        entities.clear();
        toAdd.clear();
        toRemove.clear();
        spawns.clear();
        despawns.clear();
        playableChanges.clear();

        server = null;
        match = null;
        entities = null;
        toAdd = null;
        toRemove = null;
        spawns = null;
        despawns = null;
        playableChanges = null;
    }

    /**
     * An idle tick is a tick that occurs while this {@link me.eddiep.ghost.game.match.world.World} is idle. Only the match
     * gets a tick and nothing else. All entities are considered frozen and the timeline is halted.
     */
    protected void idleTick() {
        match.tick();
    }

    /**
     * An active tick is a tick that occurs while this {@link me.eddiep.ghost.game.match.world.World} is active. All entities
     * get ticks and the current LiveMatch get a tick as well.
     */
    protected void activeTick() {
        isTicking.set(true);

        Iterator<Entity> entityIterator = entities.iterator();
        while (entityIterator.hasNext()) {
            Entity e = entityIterator.next();
            if (e.getWorld() == null)
                entityIterator.remove();
            else
                e.tick();
        }

        isTicking.set(false);

        entities.addAll(toAdd);
        toAdd.clear();

        entities.removeAll(toRemove);
        toRemove.clear();

        match.tick();

        timeline.tick();
        onTimelineTick();

        if (match.isMatchActive() && match.getTimeElapsed() - lastEntityUpdate >= UPDATE_STATE_INTERVAL) {
            lastEntityUpdate = match.getTimeElapsed();
            requestEntityUpdate();
        }
    }

    protected abstract void onTimelineTick();

    protected boolean shouldRequestTick() {
        return !(match.hasMatchEnded() && System.currentTimeMillis() - match.getMatchEnded() > 10000);
    }

    @Override
    public List<Entity> getEntities() {
        return Collections.unmodifiableList(entities);
    }

    @Override
    public WorldSnapshot takeSnapshot() {
        return WorldSnapshot.takeSnapshot(this);
    }

    @Override
    public LiveMatch getMatch() {
        return match;
    }

    @Override
    public abstract void requestEntityUpdate();

    @Override
    public Timeline getTimeline() {
        return timeline;
    }

    @Override
    public EntitySpawnSnapshot[] getSpawns() {
        if (spawns.size() == 0)
            return null;

        EntitySpawnSnapshot[] temp = spawns.toArray(new EntitySpawnSnapshot[spawns.size()]);
        spawns.clear();

        return temp;
    }

    @Override
    public EntityDespawnSnapshot[] getDespawns() {
        if (despawns.size() == 0)
            return null;

        EntityDespawnSnapshot[] temp = despawns.toArray(new EntityDespawnSnapshot[despawns.size()]);
        despawns.clear();

        return temp;
    }

    public void playableUpdated(PlayableEntity entity) {
        playableChanges.add(PlayableSnapshot.createEvent(entity));
    }

    @Override
    public PlayableSnapshot[] getPlayableChanges() {
        if (playableChanges.size() == 0)
            return null;

        PlayableSnapshot[] temp = playableChanges.toArray(new PlayableSnapshot[playableChanges.size()]);
        playableChanges.clear();

        return temp;
    }

    @Override
    public final boolean isActive() {
        return active;
    }

    @Override
    public final boolean isIdle() {
        return idle;
    }

    @Override
    public final boolean isDisposed() {
        return disposed;
    }

    @Override
    public final boolean isPaused() {
        return !idle && !active && !disposed;
    }

    @Override
    public final void idle() {
        idle = true;
        active = false;
    }

    @Override
    public final void activate() {
        idle = false;
        active = true;
    }

    @Override
    public final void pause() {
        idle = false;
        active = false;
    }
}
