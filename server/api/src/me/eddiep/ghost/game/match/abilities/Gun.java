package me.eddiep.ghost.game.match.abilities;

import me.eddiep.ghost.game.match.entities.PlayableEntity;
import me.eddiep.ghost.game.match.entities.ability.BulletEntity;
import me.eddiep.ghost.utils.Vector2f;

public class Gun implements Ability<PlayableEntity> {
    private static final float BULLET_SPEED = 16f;
    private PlayableEntity p;

    public Gun(PlayableEntity p) {
        this.p = p;
    }

    @Override
    public String name() {
        return "gun";
    }

    @Override
    public PlayableEntity owner() {
        return p;
    }

    @Override
    public void use(float targetX, float targetY, int action) {
        PlayableEntity p = owner();

        float x = p.getX();
        float y = p.getY();

        float asdx = targetX - x;
        float asdy = targetY - y;
        float inv = (float) Math.atan2(asdy, asdx);

        Vector2f velocity = new Vector2f((float)Math.cos(inv)*BULLET_SPEED, (float)Math.sin(inv)*BULLET_SPEED);

        BulletEntity b = new BulletEntity(p);
        b.setPosition(p.getPosition().cloneVector());
        b.setVelocity(velocity);

        p.getWorld().spawnEntity(b);
        p.onFire(); //Indicate this player is done firing
    }
}
