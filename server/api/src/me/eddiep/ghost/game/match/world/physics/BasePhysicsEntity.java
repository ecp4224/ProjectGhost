package me.eddiep.ghost.game.match.world.physics;

import me.eddiep.ghost.game.match.entities.BaseEntity;
import me.eddiep.ghost.game.match.entities.Entity;
import me.eddiep.ghost.game.match.world.World;
import me.eddiep.ghost.utils.PRunnable;
import me.eddiep.ghost.utils.Vector2f;
import me.eddiep.ghost.utils.VectorUtils;

public abstract class BasePhysicsEntity extends BaseEntity implements PhysicsEntity {
    private int id;
    protected Hitbox hitbox;

    public BasePhysicsEntity() {
        setName("PHYSICS ENTITY");
        sendUpdates(false);
        requestTicks(false);
    }

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
    public void setWorld(World world) {
        if (world == null) {
            super.getWorld().getPhysics().removePhysicsEntity(id);
        }
        super.setWorld(world);

        if (world != null) {
            if (hitbox == null) {
                Vector2f[] points = generateHitboxPoints();

                points = VectorUtils.rotatePoints(getRotation(), getPosition(), points);

                hitbox = new Hitbox(getName(), points);
            }

            id = world.getPhysics().addPhysicsEntity(onHit, hitbox);
        }
    }

    @Override
    public void tick() { }

    public abstract Vector2f[] generateHitboxPoints();

    public abstract void onHit(Entity entity);

    private final PRunnable<Entity> onHit = new PRunnable<Entity>() {
        @Override
        public void run(Entity p) {
            onHit(p);
        }
    };
}
