package me.eddiep.ghost.game.match.world.timeline;

import me.eddiep.ghost.game.match.entities.Entity;

public class EntityDespawnSnapshot {
    private short id;

    public static EntityDespawnSnapshot createEvent(Entity e) {
        EntityDespawnSnapshot snapshot = new EntityDespawnSnapshot();
        snapshot.id = e.getID();

        return snapshot;
    }

    private EntityDespawnSnapshot() { }

    public short getID() {
        return id;
    }
}
