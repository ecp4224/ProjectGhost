package me.eddiep.ghost.game.match.entities.map;

import me.eddiep.ghost.game.match.entities.Entity;
import me.eddiep.ghost.game.match.entities.PlayableEntity;
import me.eddiep.ghost.game.match.entities.TypeableEntity;
import me.eddiep.ghost.game.match.world.physics.BasePhysicsEntity;
import me.eddiep.ghost.game.match.world.physics.CollisionResult;
import me.eddiep.ghost.game.match.world.physics.PhysicsEntity;

public abstract class BaseLightEntity extends BasePhysicsEntity implements TypeableEntity {

    private PlayableEntity p;

    @Override
    public boolean isStaticPhysicsObject() {
        return true;
    }

    @Override
    public void onHit(CollisionResult entity) {
        PhysicsEntity collider = entity.getContacter();
        if (collider instanceof PlayableEntity) {
            p = (PlayableEntity) collider;
        }
    }

    @Override
    public void tick() {
        if(intersects(p)){
            if(!p.isVisible()){
                p.setVisible(true);
            }
        }else{
            if(p.isVisible()){
                p.setVisible(false);
            }
        }
    }

}
