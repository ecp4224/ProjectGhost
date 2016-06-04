package com.boxtrotstudio.ghost.game.match.world.timeline;

import com.boxtrotstudio.ghost.game.match.entities.Entity;
import com.boxtrotstudio.ghost.game.match.world.World;

import java.util.Arrays;
import java.util.List;

public class WorldSnapshot {
    private long snapshotTaken;
    private EntitySnapshot[] entitySnapshots;
    private EntityDespawnSnapshot[] entityDespawnSnapshots;
    private EntitySpawnSnapshot[] entitySpawnSnapshots;
    private PlayableSnapshot[] playableUpdates;
    private EventSnapshot[] events;

    public static WorldSnapshot takeSnapshot(World world, EntitySpawnSnapshot[] spawns, EntityDespawnSnapshot[] despawns, PlayableSnapshot[] playableChanges, EventSnapshot[] events) {
        WorldSnapshot snapshot = new WorldSnapshot();
        List<Entity> entities = world.getEntities();

        snapshot.entitySnapshots = new EntitySnapshot[entities.size()];

        for (int i = 0; i < snapshot.entitySnapshots.length; i++) {
            if (!entities.get(i).isSendingUpdates())
                continue;

            snapshot.entitySnapshots[i] = EntitySnapshot.takeSnapshot(entities.get(i));
        }
        snapshot.entitySpawnSnapshots = spawns;
        snapshot.entityDespawnSnapshots = despawns;
        snapshot.playableUpdates = playableChanges;
        snapshot.events = events;

        snapshot.snapshotTaken = world.getMatch().getTimeElapsed();

        return snapshot;
    }

    private WorldSnapshot() { }

    public long getSnapshotTaken() {
        return snapshotTaken;
    }

    public EntitySnapshot[] getEntitySnapshots() {
        return entitySnapshots;
    }

    public EntityDespawnSnapshot[] getEntityDespawnSnapshots() {
        return entityDespawnSnapshots;
    }

    public EntitySpawnSnapshot[] getEntitySpawnSnapshots() {
        return entitySpawnSnapshots;
    }

    public PlayableSnapshot[] getPlayableChanges() {
        return playableUpdates;
    }

    public EventSnapshot[] getEvents() {
        return events;
    }

    @Override
    public String toString() {
        return "WorldSnapshot{" +
                "snapshotTaken=" + snapshotTaken +
                ", entitySnapshots=" + Arrays.toString(entitySnapshots) +
                ", entityDespawnSnapshots=" + Arrays.toString(entityDespawnSnapshots) +
                ", entitySpawnSnapshots=" + Arrays.toString(entitySpawnSnapshots) +
                ", playableUpdates=" + Arrays.toString(playableUpdates) +
                ", events=" + Arrays.toString(events) +
                '}';
    }
}
