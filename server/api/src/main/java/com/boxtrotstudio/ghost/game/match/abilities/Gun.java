package com.boxtrotstudio.ghost.game.match.abilities;

import com.boxtrotstudio.ghost.game.match.Event;
import com.boxtrotstudio.ghost.game.match.entities.ability.BulletEntity;
import com.boxtrotstudio.ghost.utils.Vector2f;
import com.boxtrotstudio.ghost.game.match.entities.PlayableEntity;
import com.boxtrotstudio.ghost.utils.TimeUtils;

public class Gun implements Ability<PlayableEntity> {
    private static final float BULLET_SPEED = 15f;
    private static final long BASE_COOLDOWN = 555;
    private static final long ANIMATION_DELAY = 250;
    private PlayableEntity p;

    public Gun(PlayableEntity p) {
        this.p = p;
    }

    public Gun() { }

    @Override
    public String name() {
        return "gun";
    }

    @Override
    public PlayableEntity owner() {
        return p;
    }

    @Override
    public void use(float targetX, float targetY) {
        final PlayableEntity p = owner();
        p.setCanFire(false);
        p.freeze();

        float x = p.getX();
        float y = p.getY();

        float asdx = targetX - x;
        float asdy = targetY - y;
        final double direction = Math.atan2(asdy, asdx);
        final float inv = (float) direction;

        p.triggerEvent(Event.GunBegin, direction);

        TimeUtils.executeInSync(ANIMATION_DELAY, new Runnable() {
            @Override
            public void run() {
                p.triggerEvent(Event.FireGun, direction);
                Vector2f velocity = new Vector2f((float)Math.cos(inv)*BULLET_SPEED, (float)Math.sin(inv)*BULLET_SPEED);

                BulletEntity b = new BulletEntity(p);
                b.setPosition(p.getPosition().cloneVector());
                b.setVelocity(velocity);

                p.getWorld().spawnEntity(b);
                p.onFire(); //Indicate this player is done firing

                long wait = p.calculateFireRate(BASE_COOLDOWN); //Base value is 315ms
                p.unfreeze();
                TimeUtils.executeInSync(wait, new Runnable() {
                    @Override
                    public void run() {
                        p.setCanFire(true);
                    }
                }, p.getWorld());
            }
        }, p.getWorld());
    }

    @Override
    public byte id() {
        return 0;
    }
}
