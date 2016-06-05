package com.boxtrotstudio.ghost.game.match.world.physics;

import com.boxtrotstudio.ghost.utils.Vector2f;

public class CollisionResult {
    public static final CollisionResult NO_HIT = new CollisionResult(false, Vector2f.ZERO);

    private Vector2f pointOfContact;
    private PhysicsEntity contacter;
    private Hitbox collideWith;
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

    public Hitbox getCollideWith() { return collideWith; }

    public boolean didHit() {
        return didHit;
    }

    void setContacter(PhysicsEntity entity) {
        this.contacter = entity;
    }

    void setCollideWith(Hitbox hitbox) {
        this.collideWith = hitbox;
    }
}
