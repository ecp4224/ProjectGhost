package me.eddiep.ghost.game.match.world.physics;

import me.eddiep.ghost.game.match.entities.BaseEntity;
import me.eddiep.ghost.game.match.entities.Entity;
import me.eddiep.ghost.game.match.entities.PlayableEntity;
import me.eddiep.ghost.game.match.entities.ability.BulletEntity;
import me.eddiep.ghost.game.match.world.World;
import me.eddiep.ghost.utils.PRunnable;
import me.eddiep.ghost.utils.Vector2f;
import me.eddiep.ghost.utils.VectorUtils;

public abstract class BasePhysicsEntity extends BaseEntity implements PhysicsEntity {
    private int id;
    protected PolygonHitbox hitbox;

    public BasePhysicsEntity() {
        setName("PHYSICS ENTITY");
        if (isStaticPhysicsObject()) {
            sendUpdates(false);
            requestTicks(false);
        }
    }

    protected void showHitbox() {
        for (Vector2f point : hitbox.getPolygon().getPoints()) {
            BulletEntity bpoint = new BulletEntity(null);
            bpoint.setPosition(point);
            bpoint.setVelocity(new Vector2f(0f, 0f));
            bpoint.requestTicks(false);
            getWorld().spawnEntity(bpoint);
        }
    }

    public abstract boolean isStaticPhysicsObject();

    @Override
    public Hitbox getHitbox() {
        return hitbox;
    }

    @Override
    public void setPosition(Vector2f vector2f) {
        if (hitbox != null) {
            Vector2f diff = new Vector2f(vector2f.x - position.x, vector2f.y - position.y);
            hitbox.getPolygon().translate(diff);
        }
        super.setPosition(vector2f);
    }

    @Override
    public void setRotation(double rotation) {
        if (hitbox != null) {
            double diff = rotation - super.rotation;
            hitbox.getPolygon().rotate(diff);
        }

        super.setRotation(rotation);
    }
    
    @Override
    public boolean intersects(PlayableEntity player) {
        return getHitbox().isHitboxInside(player.getHitbox()).didHit();
    }

    @Override
    public void setWorld(World world) {
        if (isStaticPhysicsObject()) {
            if (world == null) {
                super.getWorld().getPhysics().removePhysicsEntity(id);
            }
            super.setWorld(world);

            if (world != null) {
                if (hitbox == null) {
                    Vector2f[] points = generateHitboxPoints();

                    points = VectorUtils.rotatePoints(getRotation(), getPosition(), points);

                    hitbox = new PolygonHitbox(getName(), points);
                    hitbox.getPolygon().saveRotation(getRotation());
                }

                id = world.getPhysics().addPhysicsEntity(onHit, onComplexHit, hitbox);
            }
        } else {
            super.setWorld(world);
        }
    }

    public Vector2f findClosestIntersectionPoint(Vector2f startPos, Vector2f endPos) {
        double d = 0;
        Vector2f point = null;
        for (Face face : hitbox.getPolygon().getFaces()) {
            Vector2f intersect = VectorUtils.pointOfIntersection(startPos, endPos, face.getPointA(), face.getPointB());
            if (intersect == null)
                continue;

            double distance = Vector2f.distance(intersect, startPos);
            if (point == null || distance < d) {
                point = intersect;
                d = distance;
            }
        }

        return point;
    }

    @Override
    public boolean intersects(PlayableEntity player) {
        return getHitbox().isHitboxInside(player.getHitbox()).didHit();
    }


    public abstract Vector2f[] generateHitboxPoints();

    public abstract void onHit(Entity entity);

    public abstract void onHit(CollisionResult entity);

    private final PRunnable<Entity> onHit = new PRunnable<Entity>() {
        @Override
        public void run(Entity p) {
            onHit(p);
        }
    };
    private final PRunnable<CollisionResult> onComplexHit = new PRunnable<CollisionResult>() {
        @Override
        public void run(CollisionResult p) {
            onHit(p);
        }
    };
}
