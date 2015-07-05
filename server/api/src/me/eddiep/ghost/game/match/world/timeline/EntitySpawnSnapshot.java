package me.eddiep.ghost.game.match.world.timeline;

import me.eddiep.ghost.game.match.entities.Entity;
import me.eddiep.ghost.game.match.entities.PlayableEntity;
import me.eddiep.ghost.game.match.entities.TypeableEntity;

public class EntitySpawnSnapshot {
    private short id;
    private String name;
    private float x, y;
    private boolean isPlayableEntity;
    private boolean isTypeableEntity;
    private byte type;

    public static EntitySpawnSnapshot createEvent(Entity e) {
        EntitySpawnSnapshot entitySpawnSnapshot = new EntitySpawnSnapshot();
        entitySpawnSnapshot.name = e.getName();
        entitySpawnSnapshot.x = e.getX();
        entitySpawnSnapshot.y = e.getY();
        entitySpawnSnapshot.id = e.getID();
        entitySpawnSnapshot.isPlayableEntity = e instanceof PlayableEntity;
        entitySpawnSnapshot.isTypeableEntity = e instanceof TypeableEntity;
        if (entitySpawnSnapshot.isTypeableEntity)
            entitySpawnSnapshot.type = ((TypeableEntity)e).getType();


        return entitySpawnSnapshot;
    }

    private EntitySpawnSnapshot() { }

    public short getID() {
        return id;
    }

    public String getName() {
        return name;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public boolean isPlayableEntity() {
        return isPlayableEntity;
    }

    public boolean isTypeableEntity() {
        return isTypeableEntity;
    }

    public byte getType() {
        return type;
    }
}
