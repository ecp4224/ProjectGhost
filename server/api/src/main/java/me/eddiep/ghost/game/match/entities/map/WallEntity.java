package me.eddiep.ghost.game.match.entities.map;

import me.eddiep.ghost.game.match.entities.Entity;
import me.eddiep.ghost.game.match.entities.PlayableEntity;
import me.eddiep.ghost.game.match.world.physics.BasePhysicsEntity;
import me.eddiep.ghost.game.match.world.physics.PolygonHitbox;
import me.eddiep.ghost.game.match.world.physics.PhysicsEntity;
import me.eddiep.ghost.utils.Vector2f;

public class WallEntity extends BasePhysicsEntity {
    private static final float WIDTH = 250f;
    private static final float HEIGHT = 128f;

    @Override
    public Vector2f[] generateHitboxPoints() {
        float x1 = getX() - (WIDTH / 2f), x2 = getX() + (WIDTH / 2f);
        float y1 = getY() - (HEIGHT / 2f), y2 = getY() + (HEIGHT / 2f);

        return new Vector2f[]{
                new Vector2f(x1, y1),
                new Vector2f(x1, y2),
                new Vector2f(x2, y2),
                new Vector2f(x2, y1)
        };
    }

    @Override
    public short getType() {
        return 80; //Items should start at -127
    }

    @Override
    public PolygonHitbox getHitbox() {
        return hitbox;
    }

    @Override
    public void onHit(Entity entity) {
        entity.getWorld().despawnEntity(entity);
    }

    @Override
    public void onHit(PhysicsEntity entity) {
        if (entity instanceof PlayableEntity) {
            entity.setVelocity(0f, 0f);
            ((PlayableEntity) entity).setTarget(null);
            //TODO Move player ?
            return;
        }

        entity.getWorld().despawnEntity(entity);
    }
}
