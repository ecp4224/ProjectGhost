package me.eddiep.ghost.client.core.game.timeline;

public class PlayableSnapshot {
    private short id;
    private byte lives;
    private boolean isDead;
    private boolean isFrozen;
    private boolean isInvincible;

    public boolean isInvincible() {
        return isInvincible;
    }

    public short getID() {
        return id;
    }

    public byte getLives() {
        return lives;
    }

    public boolean isDead() {
        return isDead;
    }

    public boolean isFrozen() {
        return isFrozen;
    }
}
