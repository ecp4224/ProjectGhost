package com.boxtrotstudio.ghost.game.match.abilities;

import com.boxtrotstudio.ghost.game.match.Event;
import com.boxtrotstudio.ghost.utils.*;
import com.boxtrotstudio.ghost.game.match.entities.PlayableEntity;
import com.boxtrotstudio.ghost.game.match.world.physics.Face;
import com.boxtrotstudio.ghost.game.match.world.physics.Hitbox;
import com.boxtrotstudio.ghost.game.match.world.physics.Polygon;

import java.util.ArrayList;
import java.util.List;

public class Laser extends CancelableAbility {
    private static final long STALL_TIME = 600L;
    private static final long FIRE_WAIT = 200L;
    private static final long ANIMATION_TIME = 350L;
    private static final long FADE_TIME = 500L;
    private static final long BASE_COOLDOWN = 315;
    private static final boolean SHOULD_BOUNCE = true;
    private PlayableEntity p;

    public Laser(PlayableEntity p) {
        this.p = p;
    }

    public Laser() { }

    @Override
    public String name() {
        return "laser";
    }

    @Override
    public PlayableEntity owner() {
        return p;
    }

    @Override
    public void onUse(float targetX, float targetY) {
        p.freeze(); //Freeze the player
        p.setVelocity(0f, 0f);
        p.setVisible(true);
        p.setCanFire(false);


        /*final LaserEntity laserEntity = new LaserEntity(p);
        laserEntity.setVisible(false);
        laserEntity.setPosition(p.getPosition());
        laserEntity.setVelocity(0f, 0f);*/

        float x = p.getX();
        float y = p.getY();

        float asdx = targetX - x;
        float asdy = targetY - y;
        final double direction = Math.atan2(asdy, asdx);
        p.triggerEvent(Event.LaserCharge, direction);
        final float inv = (float)direction;

        //laserEntity.setRotation(inv);

        //p.getWorld().spawnEntity(laserEntity);

        /*float sx = p.getX(), sy = p.getY() + 20f;
        float bx = p.getX() + 1040;
        float by = p.getY() - 20f;

        //Center of rotation
        final Vector2f[] hitbox = VectorUtils.rotatePoints(inv, p.getPosition(),
                new Vector2f(sx, sy),
                new Vector2f(bx, sy),
                new Vector2f(bx, by),
                new Vector2f(sx, by)
        );*/

        //final ArrayList<Vector2f[]> hitboxes = new ArrayList<>();
        //createRecursiveHitbox(p.getX(), p.getY(), 1040.0, (double) inv, hitboxes);

        final List<Vector2f[]> hitboxes = createHitbox(p.getX(), p.getY(), 2080.0, inv);

        //float cx = (float) (p.getX() + (Math.cos(inv) * (PlayableEntity.WIDTH / 2f)));
        //float cy = (float) (p.getY() + (Math.sin(inv) * (PlayableEntity.HEIGHT / 2f)));

        //p.getWorld().spawnParticle(ParticleEffect.CHARGE, (int)STALL_TIME, 48, cx, cy, inv);
        //p.shake(STALL_TIME);

        executeInSync(STALL_TIME, new Runnable() {
            @Override
            public void run() { //SHAKE
                canCancel = false;

                p.triggerEvent(Event.FireLaser, direction);

                final HitboxHelper.HitboxToken[] helpers = new HitboxHelper.HitboxToken[hitboxes.size()];
                TimeUtils.executeInSync(FIRE_WAIT, new Runnable() {
                    @Override
                    public void run() {
                        float distance = 0;
                        for (int i = 0; i < helpers.length; i++) {
                            helpers[i] = HitboxHelper.checkHitboxEveryTick(hitboxes.get(i), p, null, true, 90, -distance);
                            distance += Vector2f.distance(
                                    VectorUtils.midpoint(hitboxes.get(i)[0], hitboxes.get(i)[1]),
                                    VectorUtils.midpoint(hitboxes.get(i)[2], hitboxes.get(i)[3])
                            );
                        }
                    }
                }, p.getWorld());

                TimeUtils.executeInSync(ANIMATION_TIME, new Runnable() {
                    @Override
                    public void run() {

                        p.unfreeze();
                        p.onFire(); //Indicate this player is done firing

                        TimeUtils.executeInSync(FADE_TIME, new Runnable() {
                            @Override
                            public void run() {
                                for (HitboxHelper.HitboxToken h : helpers) {
                                    h.stopChecking();
                                }

                                end(BASE_COOLDOWN);
                            }
                        }, p.getWorld());
                    }
                }, p.getWorld());
            }
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
        return 1;
    }

    private List<Vector2f[]> createHitbox(float sx, float sy, double distance, double angle) {
        float endx = (float) (sx + (distance * Math.cos(angle)));
        float endy = (float) (sy + (distance * Math.sin(angle)));

        Vector2f startPoint = new Vector2f(sx, sy);
        Vector2f endPoint = new Vector2f(endx, endy);

        List<Hitbox> worldHitboxes = p.getWorld().getPhysics().allHitboxes();
        List<Vector2f[]> hitboxes = new ArrayList<>();

        String close_name = null;
        Face closestFace = null;
        Vector2f closestPoint = null;
        double close_distance = 99999999999.0;
        for (Hitbox hitbox : worldHitboxes) {
            if (!hitbox.hasPolygon() || !hitbox.isCollideable())
                continue;
            for (Face face : hitbox.getPolygon().getFaces()) {
                Vector2f pointOfIntersection = VectorUtils.pointOfIntersection(startPoint, endPoint, face.getPointA(), face.getPointB());
                if (pointOfIntersection == null)
                    continue;

                double d = Vector2f.distance(pointOfIntersection, startPoint);
                if (d == 0f)
                    continue; //This starting point is this face

                if (closestFace == null) {
                    closestFace = face;
                    closestPoint = pointOfIntersection;
                    close_distance = d;
                    close_name = hitbox.getName();
                } else if (d < close_distance) {
                    closestFace = face;
                    closestPoint = pointOfIntersection;
                    close_distance = d;
                    close_name = hitbox.getName();
                }
            }
        }

        if (closestFace == null || !SHOULD_BOUNCE) {
            float tx = startPoint.x, ty = startPoint.y + 20f;
            float bx = (float) (startPoint.x + distance);
            float by = startPoint.y - 20f;

            Vector2f[] points = VectorUtils.rotatePoints(angle, startPoint,
                    new Vector2f(tx, ty),
                    new Vector2f(bx, ty),
                    new Vector2f(bx, by),
                    new Vector2f(tx, by)
            );
            hitboxes.add(points);
            return hitboxes;
        }

        float tx = startPoint.x, ty = startPoint.y + 20f;
        float bx = (float) (startPoint.x + close_distance);
        float by = startPoint.y - 20f;
        Vector2f[] point1 = VectorUtils.rotatePoints(angle, startPoint,
                new Vector2f(tx, ty),
                new Vector2f(bx, ty),
                new Vector2f(bx, by),
                new Vector2f(tx, by)
        );

        hitboxes.add(point1);

        if (close_name.equals("MIRROR")) {
            checkMirror(closestFace, closestPoint, distance, close_distance, angle, worldHitboxes, hitboxes);
        } else if (close_name.contains("ONEWAY")) {
            checkOneWay(closestFace, closestPoint, distance, close_distance, angle, hitboxes);
        }
        return hitboxes;
    }

    private void checkMirror(Face closestFace, Vector2f closestPoint, double distance, double close_distance, double angle, List<Hitbox> worldHitboxes, List<Vector2f[]> hitboxes) {
        Vector2f temp = new Vector2f(1f, angle);
        Vector2f normal = closestFace.getNormal().cloneVector();
        Vector2f newVel = normal.scale(-2 * Vector2f.dot(temp, normal)).add(temp);
        double newAngle = Math.atan2(newVel.y, newVel.x);

        double newDistance = distance - close_distance;

        float newEndx = (float) (closestPoint.x + (newDistance * Math.cos(newAngle)));
        float newEndy = (float) (closestPoint.y + (newDistance * Math.sin(newAngle)));

        Vector2f newEndPoint = new Vector2f(newEndx, newEndy);


        double new_close_distance = -1;
        Face new_close_face = null;
        String close_name = "";
        Vector2f new_close_point = null;
        for (Hitbox hitbox : worldHitboxes) {
            if (!hitbox.hasPolygon())
                continue;

            for (Face face : hitbox.getPolygon().getFaces()) {
                Vector2f pointOfIntersection = VectorUtils.pointOfIntersection(closestPoint, newEndPoint, face.getPointA(), face.getPointB());
                if (pointOfIntersection == null)
                    continue;

                double d = Vector2f.distance(pointOfIntersection, closestPoint);
                if (d == 0f)
                    continue; //This starting point is this face

                if (new_close_distance < 0 || d < new_close_distance) {
                    new_close_distance = d;
                    new_close_face = face;
                    close_name = hitbox.getName();
                    new_close_point = pointOfIntersection;
                }
            }
        }

        if (new_close_distance < 0) {
            float mx = closestPoint.x, my = closestPoint.y + 20f;
            float mmx = (float) (closestPoint.x + newDistance), mmy = closestPoint.y - 20f;

            Vector2f[] point2 = VectorUtils.rotatePoints(newAngle, closestPoint,
                    new Vector2f(mx, my),
                    new Vector2f(mmx, my),
                    new Vector2f(mmx, mmy),
                    new Vector2f(mx, mmy)
            );

            hitboxes.add(point2);
        } else {
            float mx = closestPoint.x, my = closestPoint.y + 20f;
            float mmx = (float) (closestPoint.x + new_close_distance), mmy = closestPoint.y - 20f;

            Vector2f[] point2 = VectorUtils.rotatePoints(newAngle, closestPoint,
                    new Vector2f(mx, my),
                    new Vector2f(mmx, my),
                    new Vector2f(mmx, mmy),
                    new Vector2f(mx, mmy)
            );

            hitboxes.add(point2);

            if (close_name.equals("ONEWAY")) {
                checkOneWay(new_close_face, new_close_point, distance, new_close_distance, angle, hitboxes);
            }
        }
    }

    private void checkOneWay(Face closestFace, Vector2f closestPoint, double distance, double close_distance, double angle, List<Vector2f[]> hitboxes) {
        Polygon hitboxPolygon = closestFace.getParentPolygon();

        double temp = Math.toDegrees(angle);
        Direction laserDirection = Direction.fromDegrees(temp);

        Direction wallDirection = Direction.fromDegrees(Math.toDegrees(hitboxPolygon.getRotation() + 90));

        if (wallDirection == laserDirection) {
            float mx = closestPoint.x, my = closestPoint.y + 20f;
            float mmx = (float) (closestPoint.x + (distance - close_distance)), mmy = closestPoint.y - 20f;

            Vector2f[] point2 = VectorUtils.rotatePoints(angle, closestPoint,
                    new Vector2f(mx, my),
                    new Vector2f(mmx, my),
                    new Vector2f(mmx, mmy),
                    new Vector2f(mx, mmy)
            );

            hitboxes.add(point2);
        }
    }
}
