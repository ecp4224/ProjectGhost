package com.boxtrotstudio.ghost.game.match.entities.map;

import com.boxtrotstudio.ghost.game.match.entities.PlayableEntity;
import com.boxtrotstudio.ghost.game.match.world.physics.BasePhysicsEntity;
import com.boxtrotstudio.ghost.game.match.world.physics.PhysicsEntity;
import com.boxtrotstudio.ghost.game.match.entities.TypeableEntity;
import com.boxtrotstudio.ghost.game.match.world.physics.CollisionResult;

public abstract class BaseLightEntity extends BasePhysicsEntity implements TypeableEntity {

    private PlayableEntity p;

    @Override
    public boolean isStaticPhysicsObject() {
        return false;
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
