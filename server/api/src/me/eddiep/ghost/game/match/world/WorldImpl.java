package me.eddiep.ghost.game.match.world;

import me.eddiep.ghost.game.match.entities.Entity;
import me.eddiep.ghost.game.match.LiveMatch;
import me.eddiep.ghost.game.match.world.timeline.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static me.eddiep.ghost.utils.Constants.UPDATE_STATE_INTERVAL;

public abstract class WorldImpl implements World {
    private ArrayList<Entity> entities = new ArrayList<>();
    private ArrayList<Entity> toAdd = new ArrayList<>();
    private ArrayList<Entity> toRemove = new ArrayList<>();

    private ArrayList<EntitySpawnSnapshot> spawns = new ArrayList<>();
    private ArrayList<EntityDespawnSnapshot> despawns = new ArrayList<>();
    protected Timeline timeline;
    protected LiveMatch match;
    private AtomicBoolean isTicking = new AtomicBoolean(false);
    private long lastEntityUpdate;

    public WorldImpl(LiveMatch match) {
        this.match = match;
        this.timeline = new Timeline(this);
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
        isTicking.set(true);
        for (Entity e : entities) {
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

        if (shouldRequestTick()) {
            match.getServer().executeNextTick(new Runnable() {
                @Override
                public void run() {
                    tick();
                }
            });
        }
    }

    protected abstract void onTimelineTick();

    protected boolean shouldRequestTick() {
        return !match.hasMatchEnded();
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
            return new EntitySpawnSnapshot[0];

        EntitySpawnSnapshot[] temp = spawns.toArray(new EntitySpawnSnapshot[spawns.size()]);
        spawns.clear();

        return temp;
    }

    @Override
    public EntityDespawnSnapshot[] getDespawns() {
        if (despawns.size() == 0)
            return new EntityDespawnSnapshot[0];

        EntityDespawnSnapshot[] temp = despawns.toArray(new EntityDespawnSnapshot[despawns.size()]);
        despawns.clear();

        return temp;
    }
}
