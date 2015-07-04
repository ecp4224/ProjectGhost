package me.eddiep.ghost.game.world.timeline;

import me.eddiep.ghost.game.Entity;
import me.eddiep.ghost.game.entities.PlayableEntity;
import me.eddiep.ghost.utils.Vector2f;

public class EntitySnapshot {
    private Vector2f position;
    private Vector2f velocity;
    private int alpha;
    private double rotation;
    private boolean hasTarget;
    private Vector2f target;
    private long id;

    public static EntitySnapshot takeSnapshot(Entity e) {
        EntitySnapshot snapshot = new EntitySnapshot();
        snapshot.position = e.getPosition();
        snapshot.velocity = e.getVelocity();
        snapshot.alpha = e.getAlpha();
        snapshot.rotation = e.getRotation();
        snapshot.id = e.getID();

        if (e instanceof PlayableEntity) {
            snapshot.target = ((PlayableEntity)e).getTarget();
            snapshot.hasTarget = snapshot.target != null;
        }

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

    public boolean isHasTarget() {
        return hasTarget;
    }

    public Vector2f getTarget() {
        return target;
    }

    public long getId() {
        return id;
    }
}
