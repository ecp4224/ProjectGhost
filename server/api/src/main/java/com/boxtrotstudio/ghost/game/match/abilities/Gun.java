package com.boxtrotstudio.ghost.game.match.abilities;

import com.boxtrotstudio.ghost.game.match.Event;
import com.boxtrotstudio.ghost.game.match.entities.ability.BulletEntity;
import com.boxtrotstudio.ghost.utils.Vector2f;
import com.boxtrotstudio.ghost.game.match.entities.PlayableEntity;
import com.boxtrotstudio.ghost.utils.TimeUtils;

public class Gun extends CancelableAbility {
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
    public void onUse(float targetX, float targetY) {
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

        executeInSync(ANIMATION_DELAY, () -> {
            canCancel = false;

            p.triggerEvent(Event.FireGun, direction);
            Vector2f velocity = new Vector2f((float)Math.cos(inv)*BULLET_SPEED, (float)Math.sin(inv)*BULLET_SPEED);

            BulletEntity b = new BulletEntity(p);
            b.setPosition(p.getPosition().cloneVector());
            b.setVelocity(velocity);

            p.getWorld().spawnEntity(b);
            p.onFire(); //Indicate this player is done firing


            p.unfreeze();

            end(BASE_COOLDOWN);
        });
    }

    @Override
    protected void onCancel() {
        owner().unfreeze();
        owner().onFire();
        end(BASE_COOLDOWN);
    }

    @Override
    public byte id() {
        return 0;
    }
}
