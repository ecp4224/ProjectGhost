package com.boxtrotstudio.ghost.game.match.world.physics;

import com.boxtrotstudio.ghost.utils.PRunnable;
import com.boxtrotstudio.ghost.game.match.entities.Entity;
import com.boxtrotstudio.ghost.utils.PFunction;

import java.util.List;

public interface Physics {

    int addPhysicsEntity(PRunnable<Entity> onHit, Hitbox hitbox);

    int addPhysicsEntity(PRunnable<Entity> onHit, PRunnable<CollisionResult> onHit2, Hitbox hitbox);

    CollisionResult checkEntity(Entity entity);

    boolean foreach(PFunction<Hitbox, Boolean> onHit);

    boolean removePhysicsEntity(int id);

    List<Hitbox> allHitboxes();
}