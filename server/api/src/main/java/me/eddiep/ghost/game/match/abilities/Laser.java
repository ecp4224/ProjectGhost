package me.eddiep.ghost.game.match.abilities;

import me.eddiep.ghost.game.match.entities.PlayableEntity;
import me.eddiep.ghost.game.match.world.ParticleEffect;
import me.eddiep.ghost.game.match.world.physics.Face;
import me.eddiep.ghost.game.match.world.physics.Hitbox;
import me.eddiep.ghost.utils.HitboxHelper;
import me.eddiep.ghost.utils.TimeUtils;
import me.eddiep.ghost.utils.Vector2f;
import me.eddiep.ghost.utils.VectorUtils;

import java.util.ArrayList;
import java.util.List;

public class Laser implements Ability<PlayableEntity> {
    private static final long STALL_TIME = 600L;
    private static final long ANIMATION_TIME = 350L;
    private static final long FADE_TIME = 500L;
    private static final long BASE_COOLDOWN = 315;
    private PlayableEntity p;

    public Laser(PlayableEntity p) {
        this.p = p;
    }
    @Override
    public String name() {
        return "laser";
    }

    @Override
    public PlayableEntity owner() {
        return p;
    }

    @Override
    public void use(float targetX, float targetY, int action) {
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
        final float inv = (float) Math.atan2(asdy, asdx);

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

        final List<Vector2f[]> hitboxes = createHitbox(p.getX(), p.getY(), 1040.0, inv);

        p.getWorld().spawnParticle(ParticleEffect.CHARGE, (int)STALL_TIME, 64, p.getX(), p.getY(), inv);
        p.shake(STALL_TIME);

        TimeUtils.executeInSync(STALL_TIME, new Runnable() {
            @Override
            public void run() { //SHAKE
                p.getWorld().spawnParticle(ParticleEffect.LINE, 500, 20, p.getX(), p.getY(), inv);
                p.getWorld().requestEntityUpdate();

                final HitboxHelper.HitboxToken[] helpers = new HitboxHelper.HitboxToken[hitboxes.size()];
                for (int i = 0; i < helpers.length; i++) {
                    helpers[i] = HitboxHelper.checkHitboxEveryTick(hitboxes.get(i), p);
                }

                TimeUtils.executeInSync(ANIMATION_TIME, new Runnable() {
                    @Override
                    public void run() {
                        //laserEntity.fadeOut(500);

                        p.unfreeze();
                        p.onFire(); //Indicate this player is done firing

                        TimeUtils.executeInSync(FADE_TIME, new Runnable() {
                            @Override
                            public void run() {
                                for (HitboxHelper.HitboxToken h : helpers) {
                                    h.stopChecking();
                                }
                                //helper.stopChecking();

                                long wait = p.calculateFireRate(BASE_COOLDOWN);
                                try {
                                    Thread.sleep(wait);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                p.setCanFire(true);
                            }
                        }, p.getWorld());
                    }
                }, p.getWorld());
            }
        }, p.getWorld());
    }

    private void createRecursiveHitbox(float sx, float sy, double distance, double angle, List<Vector2f[]> currentList) {
        /*
        1. Check all hitboxes for intersection of all faces of the hitbox and get the closet
        2. Get normal of hitbox
        3. Calculate reflection vector of laser hitbox
        4. Find point of intersection and use that as new length
        5. Take remainder as the second half and create second hitbox
        6. Rotate second hitbox with same angle as reflected vector
        7. ????
        8. Profit
         */
        float endx = (float) (sx + (distance * Math.cos(angle)));
        float endy = (float) (sy + (distance * Math.sin(angle)));

        Vector2f startPoint = new Vector2f(sx, sy);
        Vector2f endPoint = new Vector2f(endx, endy);

        List<Hitbox> worldHitboxes = p.getWorld().getPhysics().allHitboxes();

        String close_name = null;
        Face closestFace = null;
        Vector2f closestPoint = null;
        double close_distance = 99999999999.0;
        for (Hitbox hitbox : worldHitboxes) {
            if (!hitbox.hasPolygon())
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

        if (closestFace == null || !close_name.equals("MIRROR")) {
            float tx = startPoint.x, ty = startPoint.y + 20f;
            float bx = (float) (startPoint.x + distance);
            float by = startPoint.y - 20f;

            final Vector2f[] hitbox = VectorUtils.rotatePoints(angle, startPoint,
                    new Vector2f(tx, ty),
                    new Vector2f(bx, ty),
                    new Vector2f(bx, by),
                    new Vector2f(tx, by)
            );
            currentList.add(hitbox);
            return;
        }

        float tx = startPoint.x, ty = startPoint.y + 20f;
        float bx = (float) (startPoint.x + close_distance), by = startPoint.y - 20f;
        final Vector2f[] hitbox = VectorUtils.rotatePoints(angle, startPoint,
                new Vector2f(tx, ty),
                new Vector2f(bx, ty),
                new Vector2f(bx, by),
                new Vector2f(tx, by)
        );
        currentList.add(hitbox);

        Vector2f temp = new Vector2f(1f, angle);
        Vector2f normal = closestFace.getNormal().cloneVector();
        Vector2f newVel = normal.scale(-2 * Vector2f.dot(temp, normal)).add(temp);
        double newAngle = Math.atan2(newVel.y, newVel.x);

        createRecursiveHitbox(closestPoint.x, closestPoint.y, distance - close_distance, newAngle, currentList);
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
            if (!hitbox.hasPolygon())
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

        if (closestFace == null || !close_name.equals("MIRROR")) {
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

        Vector2f temp = new Vector2f(1f, angle);
        Vector2f normal = closestFace.getNormal().cloneVector();
        Vector2f newVel = normal.scale(-2 * Vector2f.dot(temp, normal)).add(temp);
        double newAngle = Math.atan2(newVel.y, newVel.x);

        double newDistance = distance - close_distance;

        float newEndx = (float) (closestPoint.x + (newDistance * Math.cos(newAngle)));
        float newEndy = (float) (closestPoint.y + (newDistance * Math.sin(newAngle)));

        Vector2f newEndPoint = new Vector2f(newEndx, newEndy);


        double new_close_distance = -1;
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
            return hitboxes;
        }

        float mx = closestPoint.x, my = closestPoint.y + 20f;
        float mmx = (float) (closestPoint.x + new_close_distance), mmy = closestPoint.y - 20f;

        Vector2f[] point2 = VectorUtils.rotatePoints(newAngle, closestPoint,
                new Vector2f(mx, my),
                new Vector2f(mmx, my),
                new Vector2f(mmx, mmy),
                new Vector2f(mx, mmy)
        );

        hitboxes.add(point2);
        return hitboxes;
    }
}
