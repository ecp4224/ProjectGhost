package com.boxtrotstudio.ghost.client.core.physics;


import com.boxtrotstudio.ghost.client.core.game.SpriteEntity;
import com.boxtrotstudio.ghost.client.utils.PFunction;
import com.boxtrotstudio.ghost.client.utils.PRunnable;

import java.util.List;

public interface Physics {

    void clear();

    int addPhysicsEntity(PRunnable<SpriteEntity> onHit, Hitbox hitbox);

    int addPhysicsEntity(PRunnable<SpriteEntity> onHit, PRunnable<CollisionResult> onHit2, Hitbox hitbox);

    void checkEntity(SpriteEntity entity);

    boolean foreach(PFunction<Hitbox, Boolean> onHit);

    boolean removePhysicsEntity(int id);

    List<Hitbox> allHitboxes();
}
