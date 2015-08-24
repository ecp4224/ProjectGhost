package me.eddiep.ghost.game.match.entities.map;

import me.eddiep.ghost.game.match.entities.Entity;
import me.eddiep.ghost.game.match.entities.PlayableEntity;
import me.eddiep.ghost.game.match.world.physics.BasePhysicsEntity;
import me.eddiep.ghost.game.match.world.physics.Face;
import me.eddiep.ghost.utils.Vector2f;
import me.eddiep.ghost.utils.VectorUtils;

public class MirrorEntity extends BasePhysicsEntity {
    private static final float WIDTH = 250f;
    private static final float HEIGHT = 128f;

    public MirrorEntity() {
        super();
        setName("MIRROR");
    }

    @Override
    public Vector2f[] generateHitboxPoints() {
        float x1 = getX() - (WIDTH / 2f), x2 = getX() + (WIDTH / 2f);
        float y1 = getY() - (HEIGHT / 2f), y2 = getY() + (HEIGHT / 2f);

        return new Vector2f[] {
                new Vector2f(x1, y1),
                new Vector2f(x1, y2),
                new Vector2f(x2, y2),
                new Vector2f(x2, y1)
        };
    }

    @Override
    public void onHit(Entity entity) {
        if (entity instanceof PlayableEntity) {
            entity.setVelocity(0f, 0f);
            ((PlayableEntity) entity).setTarget(null);
            return;
        }

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
    }

    @Override
    public byte getType() {
        return 81; //Items should start at -127
    }
}
