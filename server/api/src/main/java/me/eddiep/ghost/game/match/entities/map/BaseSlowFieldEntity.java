package me.eddiep.ghost.game.match.entities.map;

import me.eddiep.ghost.game.match.entities.Entity;
import me.eddiep.ghost.game.match.entities.PlayableEntity;
import me.eddiep.ghost.game.match.entities.TypeableEntity;
import me.eddiep.ghost.game.match.stats.BuffType;
import me.eddiep.ghost.game.match.world.physics.BasePhysicsEntity;
import me.eddiep.ghost.game.match.world.physics.CollisionResult;
import me.eddiep.ghost.game.match.world.physics.PhysicsEntity;

public abstract class BaseSlowFieldEntity extends BasePhysicsEntity implements TypeableEntity {
    public static final int SLOW_PERCENT = 40;

    @Override
    public boolean isStaticPhysicsObject() {
        return true;
    }

    @Override
    public void onHit(Entity entity) { }

    @Override
    public void onHit(CollisionResult entity) {
        PhysicsEntity collider = entity.getContacter();
        if (collider instanceof PlayableEntity) {
            PlayableEntity p = (PlayableEntity)collider;

            if (!p.getSpeedStat().hasBuff("SLOW_FIELD")) {
                p.getSpeedStat().addTimedBuff("SLOW_FIELD", BuffType.PercentSubtraction, SLOW_PERCENT, false, 1.0);
            }
        }
    }
}
