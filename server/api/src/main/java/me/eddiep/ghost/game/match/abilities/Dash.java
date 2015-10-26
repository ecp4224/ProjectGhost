package me.eddiep.ghost.game.match.abilities;

import me.eddiep.ghost.game.match.entities.PlayableEntity;
import me.eddiep.ghost.game.match.world.physics.Face;
import me.eddiep.ghost.game.match.world.physics.Hitbox;
import me.eddiep.ghost.utils.*;

import java.util.List;

public class Dash implements Ability<PlayableEntity> {
    private static final long BASE_COOLDOWN = 315;
    private PlayableEntity p;

    private static final float SPEED_DECREASE = 0.8f;
    private static final int STALL = 800;

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

        final Vector2f target = calculateDash(x, y, targetX, targetY, inv);

        final double distance = Vector2f.distance(p.getPosition(), target);

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
                p.setTarget(target);

                //Create a HitboxHelper to check the dash hitbox every server tick
                final HitboxHelper.HitboxToken hitboxToken = HitboxHelper.checkHitboxEveryTick(
                        hitbox,               //The hitbox to check
                        p                     //The damager
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
                        long wait = p.calculateFireRate(BASE_COOLDOWN);
                        TimeUtils.executeInSync(wait, new Runnable() {
                            @Override
                            public void run() {
                                p.setCanFire(true);
                            }
                        }, p.getWorld());
                    }
                }, new PFunction<Void, Boolean>() {
                    @Override
                    public Boolean run(Void val) {
                        double dis = Vector2f.distance(p.getPosition(), new Vector2f(x, y));
                        return dis >= distance || p.getTarget() == null; //If the target is null, then they collided with something..
                    }
                }, p.getWorld());
            }
        }, p.getWorld());
    }

    private Vector2f calculateDash(float x, float y, float targetX, float targetY, double angle) {
        Vector2f startPos = new Vector2f(x, y);
        Vector2f endPos = new Vector2f(targetX, targetY);

        List<Hitbox> hitboxList = p.getWorld().getPhysics().allHitboxes();

        double distance = 0;
        Vector2f closePoint = null;
        Face closeFace = null;
        String name = "";
        for (Hitbox hitbox : hitboxList) {
            if (!hitbox.hasPolygon())
                continue;
            for (Face face : hitbox.getPolygon().getFaces()) {
                Vector2f intersect = VectorUtils.pointOfIntersection(startPos, endPos, face.getPointA(), face.getPointB());
                if (intersect == null)
                    continue;

                double d = Vector2f.distance(startPos, intersect);
                if (closePoint == null || distance < d) {
                    distance = d;
                    closePoint = intersect;
                    closeFace = face;
                    name = hitbox.getName();
                }
            }
        }

        if (closePoint == null) {
            return new Vector2f(targetX, targetY);
        }

        if (name.equals("ONEWAY")) {
            Direction dashDirection = Direction.fromDegrees(Math.toDegrees(angle));

            Direction wallDirection = Direction.fromDegrees(Math.toDegrees(closeFace.getParentPolygon().getRotation()) + 90);

            if (dashDirection == wallDirection) {
                return calculateDash(closePoint.getX(), closePoint.getY(), targetX, targetY, angle);
            }
        }

        return closePoint;
    }
}
