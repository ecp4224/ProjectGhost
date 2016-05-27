package com.boxtrotstudio.ghost.game.match.entities.map;

import com.boxtrotstudio.ghost.game.match.entities.Entity;
import com.boxtrotstudio.ghost.game.match.entities.PlayableEntity;
import com.boxtrotstudio.ghost.game.match.entities.TypeableEntity;
import com.boxtrotstudio.ghost.game.match.stats.BuffType;
import com.boxtrotstudio.ghost.game.match.world.World;
import com.boxtrotstudio.ghost.game.match.world.physics.BasePhysicsEntity;
import com.boxtrotstudio.ghost.game.match.world.physics.CollisionResult;
import com.boxtrotstudio.ghost.game.match.world.physics.PhysicsEntity;

public abstract class BaseSlowFieldEntity extends BasePhysicsEntity implements TypeableEntity {
    public static final int SLOW_PERCENT = 40;

    @Override
    public boolean isStaticPhysicsObject() {
        return true;
    }

    @Override
    public void onHit(Entity entity) { }

    @Override
    public void setWorld(World world) {
        super.setWorld(world);

        hitbox.setCollideable(false);
    }

    @Override
    public void onHit(CollisionResult entity) {
        PhysicsEntity collider = entity.getContacter();
        if (collider instanceof PlayableEntity) {
            PlayableEntity p = (PlayableEntity)collider;

            if (!p.getSpeedStat().hasBuff("SLOW_FIELD")) {
                p.getSpeedStat().addTimedBuff("SLOW_FIELD", BuffType.PercentSubtraction, SLOW_PERCENT, false, 1.0);
                p.onStatUpdate(p.getSpeedStat());
            }
        }
    }
}
