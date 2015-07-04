package me.eddiep.ghost.game.world;

import me.eddiep.ghost.game.Entity;
import me.eddiep.ghost.game.LiveMatch;
import me.eddiep.ghost.game.world.timeline.Timeline;
import me.eddiep.ghost.game.world.timeline.WorldSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class WorldImpl implements World {
    private ArrayList<Entity> entities = new ArrayList<>();
    private ArrayList<Entity> toAdd = new ArrayList<>();
    private ArrayList<Entity> toRemove = new ArrayList<>();

    private Timeline timeline;
    private LiveMatch match;
    private AtomicBoolean isTicking = new AtomicBoolean(false);

    public WorldImpl(LiveMatch match) {
        this.match = match;
        this.timeline = new Timeline(this);
    }

    @Override
    public void spawnEntity(Entity entity) {
        if (isTicking.get()) {
            toAdd.add(entity);
        } else {
            entities.add(entity);
        }
    }

    @Override
    public void despawnEntity(Entity entity) {
        if (isTicking.get()) {
            toRemove.add(entity);
        } else {
            entities.remove(entity);
        }
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

        timeline.tick();
    }

    @Override
    public abstract void onLoad();

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
}
