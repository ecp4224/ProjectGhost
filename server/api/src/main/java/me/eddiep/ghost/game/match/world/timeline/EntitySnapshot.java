package me.eddiep.ghost.game.match.world.timeline;

import me.eddiep.ghost.game.match.entities.Entity;
import me.eddiep.ghost.game.match.entities.PlayableEntity;
import me.eddiep.ghost.game.match.entities.TypeableEntity;
import me.eddiep.ghost.game.match.entities.playable.impl.BaseNetworkPlayer;
import me.eddiep.ghost.utils.Vector2f;

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

    public static EntitySnapshot takeSnapshot(Entity e) {
        EntitySnapshot snapshot = new EntitySnapshot();
        snapshot.x = e.getX();
        snapshot.y = e.getY();
        //snapshot.position = e.getPosition().cloneVector();
        snapshot.velX = e.getXVelocity();
        snapshot.velY = e.getYVelocity();
        //snapshot.velocity = e.getVelocity().cloneVector();
        snapshot.alpha = e.getAlpha();
        snapshot.rotation = e.getRotation();
        snapshot.id = e.getID();
        snapshot.isPlayer = e instanceof BaseNetworkPlayer;
        snapshot.name = e.getName();

        if (e instanceof PlayableEntity) {
            //snapshot.target = ((PlayableEntity)e).getTarget();
            snapshot.hasTarget = ((PlayableEntity) e).getTarget() != null;
            snapshot.isPlayableEntity = true;
            snapshot.allyVisible = ((PlayableEntity)e).visibleToAllies();

            if (snapshot.hasTarget) {
                snapshot.targetX = ((PlayableEntity) e).getTarget().x;
                snapshot.targetY = ((PlayableEntity) e).getTarget().y;
            }
        } else if (e instanceof TypeableEntity) {
            snapshot.isTypeableEntity = true;
            snapshot.type = ((TypeableEntity)e).getType();
        }

        /*
        entitySpawnSnapshot.name = e.getName();
        entitySpawnSnapshot.x = e.getX();
        entitySpawnSnapshot.y = e.getY();
        entitySpawnSnapshot.id = e.getID();
        entitySpawnSnapshot.isPlayableEntity = e instanceof PlayableEntity;
        entitySpawnSnapshot.isTypeableEntity = e instanceof TypeableEntity;
        if (entitySpawnSnapshot.isTypeableEntity)
            entitySpawnSnapshot.type = ((TypeableEntity)e).getType();
         */

        return snapshot;
    }

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
