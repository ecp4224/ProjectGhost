package me.eddiep.ghost.client.core.physics;


import me.eddiep.ghost.client.utils.Vector2f;

public class CollisionResult {
    public static final CollisionResult NO_HIT = new CollisionResult(false, Vector2f.ZERO);

    private Vector2f pointOfContact;
    private PhysicsEntity contacter;
    private boolean didHit;

    public CollisionResult(boolean result, Vector2f pointOfContact) {
        this.didHit = result;
        this.pointOfContact = pointOfContact;
    }

    public Vector2f getPointOfContact() {
        return pointOfContact;
    }

    public PhysicsEntity getContacter() {
        return contacter;
    }

    public boolean didHit() {
        return didHit;
    }

    void setContacter(PhysicsEntity entity) {
        this.contacter = entity;
    }
}
