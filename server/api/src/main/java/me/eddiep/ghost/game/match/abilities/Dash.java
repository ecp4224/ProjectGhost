package me.eddiep.ghost.game.match.abilities;

import me.eddiep.ghost.game.match.entities.PlayableEntity;
import me.eddiep.ghost.utils.*;

public class Dash implements Ability<PlayableEntity> {
    private PlayableEntity p;

    private static final float SPEED_DECREASE = 0.8f;
    private static final int STALL = 600;

    public Dash(PlayableEntity p) {
        this.p = p;
    }

    @Override
    public String name() {
        return "Dash";
    }

    @Override
    public PlayableEntity owner() {
        return p;
    }

    @Override
    public void use(final float targetX, final float targetY, int actionRequested) {
        p.setCanFire(false);
        p.setVisible(true);

        final float old_speed = p.getSpeed();
        p.setSpeed(p.getSpeed() - (p.getSpeed() * SPEED_DECREASE));

        final float x = p.getX();
        final float y = p.getY();

        float asdx = targetX - x;
        float asdy = targetY - y;
        final float inv = (float) Math.atan2(asdy, asdx);

        final double distance = Vector2f.distance(p.getPosition(), new Vector2f(targetX, targetY));

        float sx = p.getX(), sy = p.getY() + 32f;
        float bx = (float) (p.getX() + distance);
        float by = p.getY() - 32f;

                                                           //Center of rotation
        final Vector2f[] hitbox = VectorUtils.rotatePoints(inv, p.getPosition(),
                new Vector2f(sx, sy),
                new Vector2f(bx, sy),
                new Vector2f(bx, by),
                new Vector2f(sx, by)
        );

        TimeUtils.executeInSync(STALL, new Runnable() {
            @Override
            public void run() {
                p.freeze();
                p.setSpeed(50f);
                p.setTarget(new Vector2f(targetX, targetY));

                //Create a HitboxHelper to check the dash hitbox every server tick
                final HitboxHelper.HitboxToken hitboxToken = HitboxHelper.checkHitboxEveryTick(
                        hitbox,               //The hitbox to check
                        p,                    //The damager
                        Global.DEFAULT_SERVER //The server to tick on
                );

                TimeUtils.executeWhen(new Runnable() {
                    @Override
                    public void run() {
                        //Stop checking this hitbox
                        hitboxToken.stopChecking();

                        p.setSpeed(old_speed);
                        p.setTarget(null);
                        p.unfreeze();
                        p.onFire();
                        p.setCanFire(true);
                    }
                }, new PFunction<Void, Boolean>() {
                    @Override
                    public Boolean run(Void val) {
                        return Vector2f.distance(p.getPosition(), new Vector2f(x, y)) >= distance;
                    }
                }, Global.DEFAULT_SERVER);
            }
        });
    }
}
