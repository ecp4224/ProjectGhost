package me.eddiep.ghost.game.match.entities.map;

import me.eddiep.ghost.game.match.entities.Entity;
import me.eddiep.ghost.game.match.entities.PlayableEntity;
import me.eddiep.ghost.game.match.entities.TypeableEntity;
import me.eddiep.ghost.game.match.world.physics.BasePhysicsEntity;
import me.eddiep.ghost.game.match.world.physics.CollisionResult;
import me.eddiep.ghost.game.match.world.physics.Face;
import me.eddiep.ghost.game.match.world.physics.PhysicsEntity;
import me.eddiep.ghost.utils.Vector2f;
import me.eddiep.ghost.utils.VectorUtils;

public class MirrorEntity extends BasePhysicsEntity implements TypeableEntity {
    public MirrorEntity() {
        super();
        setName("MIRROR");
    }

    @Override
    public boolean isStaticPhysicsObject() {
        return true;
    }

    @Override
    public Vector2f[] generateHitboxPoints() {
        float x1 = getX() - (width / 2f), x2 = getX() + (width / 2f);
        float y1 = getY() - (height / 2f), y2 = getY() + (height / 2f);

        return new Vector2f[] {
                new Vector2f(x1, y1),
                new Vector2f(x1, y2),
                new Vector2f(x2, y2),
                new Vector2f(x2, y1)
        };
    }

    @Override
    public void onHit(Entity entity) {
        if (entity.doesBounce()) {

            Vector2f oldPoint = new Vector2f(entity.getPosition().x - (entity.getVelocity().x * 1.5f), entity.getPosition().y - (entity.getVelocity().y * 1.5f));

            Vector2f endPoint = new Vector2f(oldPoint.x + (entity.getVelocity().x * 50), oldPoint.y + (entity.getVelocity().y * 50));

            Face closestFace = null;
            Vector2f closestPoint = null;
            double distance = 99999999999.0;
            for (Face face : hitbox.getPolygon().getFaces()) {
                Vector2f pointOfIntersection = VectorUtils.pointOfIntersection(oldPoint, endPoint, face.getPointA(), face.getPointB());
                if (pointOfIntersection == null)
                    continue;

                double d = Vector2f.distance(pointOfIntersection, oldPoint);
                if (closestFace == null) {
                    closestFace = face;
                    closestPoint = pointOfIntersection;
                    distance = d;
                } else if (d < distance) {
                    closestFace = face;
                    closestPoint = pointOfIntersection;
                    distance = d;
                }
            }

            if (closestFace == null) {
                //entity.getWorld().despawnEntity(entity); //wat
                return;
            }

            Vector2f normal = closestFace.getNormal().cloneVector();
            Vector2f newVel = normal.scale(-2 * Vector2f.dot(entity.getVelocity(), normal)).add(entity.getVelocity());
            entity.setVelocity(newVel);

            entity.setPosition(new Vector2f(closestPoint.x, closestPoint.y));
        } else {
            entity.onCollision(this);
        }
    }

    @Override
    public void onHit(CollisionResult result) {
        PhysicsEntity entity = result.getContacter();
        if (entity instanceof PlayableEntity) {
            Vector2f contactPoint = result.getPointOfContact();
            Vector2f startPos = new Vector2f(contactPoint.x - (entity.getXVelocity() * 1.5f), contactPoint.y - (entity.getYVelocity() * 1.5f));
            Vector2f endPos = new Vector2f(contactPoint.x + (entity.getXVelocity() * 100f), contactPoint.y + (entity.getYVelocity() * 100f));

            Vector2f intersect = findClosestIntersectionPoint(startPos, endPos);

            if (intersect == null)
                return;

            float x = entity.getX();
            float y = entity.getY();

            float asdx = x - intersect.x;
            float asdy = y - intersect.y;
            double inv =  Math.atan2(asdy, asdx);

            double distance = Vector2f.distance(contactPoint, intersect) + 10.0;

            entity.setPosition(new Vector2f((float)(x + (distance * Math.cos(inv))), (float)(y + (distance * Math.sin(inv)))));

            entity.setVelocity(0f, 0f);
            ((PlayableEntity) entity).setTarget(null);

            entity.getWorld().requestEntityUpdate();
            return;
        }

        //TODO Better bounce ?
        onHit(entity);
    }

    @Override
    public short getType() {
        return 81; //Items should start at -127
    }
}
