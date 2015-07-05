package me.eddiep.ghost.game.match.world.timeline;

import me.eddiep.ghost.game.match.entities.Entity;
import me.eddiep.ghost.game.match.world.World;

import java.util.List;

public class WorldSnapshot {
    private long snapshotTaken;
    private EntitySnapshot[] entitySnapshots;
    private EntityDespawnSnapshot[] entityDespawnSnapshots;
    private EntitySpawnSnapshot[] entitySpawnSnapshots;

    public static WorldSnapshot takeSnapshot(World world) {
        WorldSnapshot snapshot = new WorldSnapshot();
        List<Entity> entities = world.getEntities();
        snapshot.entitySnapshots = new EntitySnapshot[entities.size()];

        for (int i = 0; i < snapshot.entitySnapshots.length; i++) {
            snapshot.entitySnapshots[i] = EntitySnapshot.takeSnapshot(entities.get(i));
        }
        snapshot.entitySpawnSnapshots = world.getSpawns();
        snapshot.entityDespawnSnapshots = world.getDespawns();

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
}
