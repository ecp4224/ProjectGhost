package me.eddiep.ghost.game.match.entities.map;

import me.eddiep.ghost.game.match.entities.Entity;
import me.eddiep.ghost.game.match.entities.PlayableEntity;
import me.eddiep.ghost.game.match.entities.TypeableEntity;
import me.eddiep.ghost.game.match.world.physics.BasePhysicsEntity;
import me.eddiep.ghost.game.match.world.physics.CollisionResult;
import me.eddiep.ghost.game.match.world.physics.PhysicsEntity;
import me.eddiep.ghost.utils.Direction;
import me.eddiep.ghost.utils.Vector2f;

public class OneWayMirrorEntity extends BasePhysicsEntity implements TypeableEntity {

    public OneWayMirrorEntity() {
        super();
        setName("ONEWAY");
    }

    @Override
    public void setRotation(double rotation) {
        super.setRotation(rotation);
        setName("ONEWAY:" + rotation);
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
        if (entity.getVelocity().isZero()) {
            return;
        }

        Direction facing = Direction.fromDegrees(Math.toDegrees(rotation) + 90);

        Vector2f normalVel = entity.getVelocity().cloneVector();
        normalVel.normalise();

        Direction velocityDirection = normalVel.getDirection();

        if (velocityDirection != facing) {
            entity.onCollision(this);
        }
    }

    @Override
    public void onHit(CollisionResult result) {
        PhysicsEntity entity = result.getContacter();

        if (entity.getVelocity().isZero()) {
            return;
        }

        Direction facing = Direction.fromDegrees(Math.toDegrees(rotation) + 90);

        Vector2f normalVel = entity.getVelocity().cloneVector();
        normalVel.normalise();

        Direction velocityDirection = normalVel.getDirection();

        if (velocityDirection != facing) {
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
            entity.onCollision(this);
        }
    }

    @Override
    public short getType() {
        return 82;
    }
}
