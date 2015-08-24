package me.eddiep.ghost.game.match.world.timeline;

import me.eddiep.ghost.game.match.entities.Entity;
import me.eddiep.ghost.game.match.entities.PlayableEntity;
import me.eddiep.ghost.game.match.entities.TypeableEntity;
import me.eddiep.ghost.game.match.entities.playable.impl.BaseNetworkPlayer;
import me.eddiep.ghost.utils.Vector2f;

public class EntitySnapshot {
    private Vector2f position;
    private Vector2f velocity;
    private int alpha;
    private double rotation;
    private boolean hasTarget;
    private Vector2f target;
    private short id;
    private boolean isPlayer;

    private String name;
    private boolean isPlayableEntity;
    private boolean isTypeableEntity;
    private byte type;

    public static EntitySnapshot takeSnapshot(Entity e) {
        EntitySnapshot snapshot = new EntitySnapshot();
        snapshot.position = e.getPosition().cloneVector();
        snapshot.velocity = e.getVelocity().cloneVector();
        snapshot.alpha = e.getAlpha();
        snapshot.rotation = e.getRotation();
        snapshot.id = e.getID();
        snapshot.isPlayer = e instanceof BaseNetworkPlayer;
        snapshot.name = e.getName();

        if (e instanceof PlayableEntity) {
            snapshot.target = ((PlayableEntity)e).getTarget();
            snapshot.hasTarget = snapshot.target != null;
            snapshot.isPlayableEntity = true;

            if (snapshot.hasTarget) {
                snapshot.target = snapshot.target.cloneVector();
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


    public Vector2f getPosition() {
        return position;
    }

    public Vector2f getVelocity() {
        return velocity;
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

    public Vector2f getTarget() {
        return target;
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
        entitySpawnSnapshot.x = position.x;
        entitySpawnSnapshot.y = position.y;
        entitySpawnSnapshot.id = id;
        entitySpawnSnapshot.isPlayableEntity = isPlayableEntity;
        entitySpawnSnapshot.isTypeableEntity = isTypeableEntity;
        entitySpawnSnapshot.type = type;
        entitySpawnSnapshot.rotation = rotation;

        return entitySpawnSnapshot;
    }

    @Override
    public String toString() {
        return "EntitySnapshot{" +
                "position=" + position +
                ", velocity=" + velocity +
                ", alpha=" + alpha +
                ", rotation=" + rotation +
                ", hasTarget=" + hasTarget +
                ", target=" + target +
                ", id=" + id +
                ", isPlayer=" + isPlayer +
                ", name='" + name + '\'' +
                ", isPlayableEntity=" + isPlayableEntity +
                ", isTypeableEntity=" + isTypeableEntity +
                ", type=" + type +
                '}';
    }
}
