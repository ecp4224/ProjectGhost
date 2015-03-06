package me.eddiep.ghost.server.game;

import me.eddiep.ghost.server.game.util.Vector2f;
import me.eddiep.ghost.server.utils.events.EventEmitter;

public abstract class Entity extends EventEmitter {
    protected Vector2f position;
    protected Vector2f velocity;
    protected Entity parent;
    protected Match containingMatch;
    private byte ID;

    public Match getMatch() {
        return containingMatch;
    }

    public void setMatch(Match containingMatch) {
        this.containingMatch = containingMatch;
    }

    public Entity getParent() {
        return parent;
    }

    public void setParent(Entity parent) {
        this.parent = parent;
    }

    public Vector2f getPosition() {
        return position;
    }

    public void setPosition(Vector2f position) {
        this.position = position;
    }

    public Vector2f getVelocity() {
        return velocity;
    }

    public void setVelocity(Vector2f velocity) {
        this.velocity = velocity;
    }

    public float getX() {
        return position.x;
    }

    public float getY() {
        return position.y;
    }

    public float getXVelocity() {
        return velocity.x;
    }

    public float getYVelocity() {
        return velocity.y;
    }

    public void setVelocity(float xvel, float yvel) {
        setVelocity(new Vector2f(xvel, yvel));
    }

    public abstract void tick();

    void setID(byte ID) {
        this.ID = ID;
    }

    public byte getID() {
        return ID;
    }

    public boolean isInside(float xmin, float ymin, float xmax, float ymax) {
        return position.x >= xmin && position.y >= ymin && position.x <= xmax && position.x <= ymax;
    }
}
