package me.eddiep.ghost.game.match.world.timeline;

import me.eddiep.ghost.game.match.entities.PlayableEntity;

public class PlayableSnapshot {
    private short id;
    private byte lives;
    private boolean isDead;
    private boolean isFrozen;
    private boolean isInvincible;

    public static PlayableSnapshot createEvent(PlayableEntity entity) {
        PlayableSnapshot snapshot = new PlayableSnapshot();
        snapshot.id = entity.getID();
        snapshot.lives = entity.getLives();
        snapshot.isDead = entity.isDead();
        snapshot.isFrozen = entity.isFrozen();
        snapshot.isInvincible = entity.isInvincible();

        return snapshot;
    }

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
