package me.eddiep.ghost.client.core.game.timeline;

public class EntitySpawnSnapshot {
    short id;
    String name;
    float x, y;
    double rotation;
    boolean isPlayableEntity;
    boolean isTypeableEntity;
    boolean isParticle;
    short type;

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
}
