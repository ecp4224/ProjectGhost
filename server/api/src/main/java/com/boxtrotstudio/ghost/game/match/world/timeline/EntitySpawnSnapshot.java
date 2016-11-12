package com.boxtrotstudio.ghost.game.match.world.timeline;

import com.boxtrotstudio.ghost.game.match.entities.Entity;
import com.boxtrotstudio.ghost.game.match.entities.PlayableEntity;
import com.boxtrotstudio.ghost.game.match.entities.TypeableEntity;
import com.boxtrotstudio.ghost.game.match.world.ParticleEffect;

public class EntitySpawnSnapshot {
    short id;
    String name;
    float x, y;
    short width = -1, height = -1;
    double rotation;
    boolean isPlayableEntity;
    boolean isTypeableEntity;
    boolean isParticle;
    short type;
    boolean isStatic;
    boolean hasLighting;

    public static EntitySpawnSnapshot createParticleEvent(ParticleEffect effect, int duration, int size, float x, float y, double rotation) {
        EntitySpawnSnapshot snapshot = new EntitySpawnSnapshot();
        snapshot.type = effect.getId();
        snapshot.x = x;
        snapshot.y = y;
        snapshot.isParticle = true;
        snapshot.rotation = rotation;
        snapshot.name = duration + ":" + size + ":" + rotation; //Store these 3 values in the name field

        return snapshot;
    }

    public static EntitySpawnSnapshot createEvent(Entity e) {
        EntitySpawnSnapshot entitySpawnSnapshot = new EntitySpawnSnapshot();
        entitySpawnSnapshot.name = e.getName();
        entitySpawnSnapshot.x = e.getX();
        entitySpawnSnapshot.y = e.getY();
        entitySpawnSnapshot.id = e.getID();
        entitySpawnSnapshot.width = e.getWidth();
        entitySpawnSnapshot.height = e.getHeight();
        entitySpawnSnapshot.rotation = e.getRotation();
        entitySpawnSnapshot.isPlayableEntity = e instanceof PlayableEntity;
        entitySpawnSnapshot.isTypeableEntity = e instanceof TypeableEntity;
        entitySpawnSnapshot.hasLighting = e.hasLighting();
        entitySpawnSnapshot.isStatic = !e.isSendingUpdates(); //If this entity is not requesting ticks, then don't save the snapshot
        if (entitySpawnSnapshot.isTypeableEntity)
            entitySpawnSnapshot.type = ((TypeableEntity)e).getType();


        return entitySpawnSnapshot;
    }

    EntitySpawnSnapshot() { }

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

    public short getType() {
        return type;
    }

    public boolean isParticle() {
        return isParticle;
    }

    public double getRotation() {
        return rotation;
    }

    public short getWidth() {
        return width;
    }

    public short getHeight() {
        return height;
    }

    public boolean hasLighting() {
        return hasLighting;
    }
}
