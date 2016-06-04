package com.boxtrotstudio.ghost.game.match.entities.map;

import com.boxtrotstudio.ghost.game.match.entities.Entity;
import com.boxtrotstudio.ghost.game.match.entities.PlayableEntity;
import com.boxtrotstudio.ghost.game.match.entities.TypeableEntity;
import com.boxtrotstudio.ghost.game.match.world.physics.BasePhysicsEntity;
import com.boxtrotstudio.ghost.game.match.world.physics.CollisionResult;
import com.boxtrotstudio.ghost.game.match.world.physics.PhysicsEntity;
import com.boxtrotstudio.ghost.utils.Vector2f;

public class BottomlessPitEntity extends BasePhysicsEntity implements TypeableEntity {
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
    public void onHit(Entity entity) { }

    @Override
    public void onHit(CollisionResult entity) {
        PhysicsEntity collider = entity.getContacter();
        if (collider instanceof PlayableEntity) {
            PlayableEntity p = (PlayableEntity)collider;

            p.kill();
        }
    }

    @Override
    public short getType() {
        return 85;
    }
}
