package com.boxtrotstudio.ghost.client.core.physics;


import com.boxtrotstudio.ghost.client.utils.PFunction;
import com.boxtrotstudio.ghost.client.utils.PRunnable;
import com.boxtrotstudio.ghost.client.core.game.Entity;

import java.util.List;

public interface Physics {

    int addPhysicsEntity(PRunnable<Entity> onHit, Hitbox hitbox);

    int addPhysicsEntity(PRunnable<Entity> onHit, PRunnable<CollisionResult> onHit2, Hitbox hitbox);

    void checkEntity(Entity entity);

    boolean foreach(PFunction<Hitbox, Boolean> onHit);

    boolean removePhysicsEntity(int id);

    List<Hitbox> allHitboxes();
}
