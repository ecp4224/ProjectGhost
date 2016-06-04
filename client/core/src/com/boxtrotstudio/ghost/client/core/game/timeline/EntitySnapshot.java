package com.boxtrotstudio.ghost.client.core.game.timeline;

import com.boxtrotstudio.ghost.client.utils.Vector2f;

public class EntitySnapshot {
    public Vector2f position;
    public Vector2f target;
    private float x, y, velX, velY, targetX, targetY;
    private int alpha;
    private double rotation;
    private boolean hasTarget;
    private short id;
    private boolean isPlayer;

    private String name;
    private boolean isPlayableEntity;
    private boolean isTypeableEntity;
    private short type;
    private boolean allyVisible;

    private EntitySnapshot() { }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getVelX() {
        return velX;
    }

    public float getVelY() {
        return velY;
    }

    public float getTargetX() {
        return targetX;
    }

    public float getTargetY() {
        return targetY;
    }

    public int getAlpha() {
        return alpha;
    }

    public double getRotation() {
        return rotation;
    }

    public boolean hasTarget() {
        return hasTarget;
    }

    public boolean isVisibleToAllies() {
        return allyVisible;
    }

    public boolean isPlayer() {
        return isPlayer;
    }

    public short getID() {
        return id;
    }

    public EntitySpawnSnapshot toSpawnSnapshot() {
        EntitySpawnSnapshot entitySpawnSnapshot = new EntitySpawnSnapshot();

        entitySpawnSnapshot.name = name;
        entitySpawnSnapshot.x = x;
        entitySpawnSnapshot.y = y;
        entitySpawnSnapshot.id = id;
        entitySpawnSnapshot.isPlayableEntity = isPlayableEntity;
        entitySpawnSnapshot.isTypeableEntity = isTypeableEntity;
        entitySpawnSnapshot.type = type;
        entitySpawnSnapshot.rotation = rotation;

        return entitySpawnSnapshot;
    }
}
