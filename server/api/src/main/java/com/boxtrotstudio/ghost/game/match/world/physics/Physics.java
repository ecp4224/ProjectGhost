package com.boxtrotstudio.ghost.game.match.world.physics;

import com.boxtrotstudio.ghost.game.match.entities.Entity;
import com.boxtrotstudio.ghost.utils.PFunction;
import com.boxtrotstudio.ghost.utils.PRunnable;
import com.boxtrotstudio.ghost.utils.Vector2f;

import java.util.List;

public interface Physics {

    int addPhysicsEntity(PRunnable<Entity> onHit, Hitbox hitbox);

    int addPhysicsEntity(PRunnable<Entity> onHit, PRunnable<CollisionResult> onHit2, Hitbox hitbox);

    CollisionResult checkEntity(Entity entity);

    boolean foreach(PFunction<Hitbox, Boolean> onHit);

    /**
     * Returns true if this line intersects with any hitbox
     * @param startPoint The start point of this line
     * @param endPoint The end point of this line
     * @return True if this line intersects with any hitbox, false otherwise
     */
    boolean projectLine(Vector2f startPoint, Vector2f endPoint);

    boolean removePhysicsEntity(int id);

    List<Hitbox> allHitboxes();
}