package me.eddiep.ghost.game.match.world.physics;

import me.eddiep.ghost.game.match.entities.Entity;
import me.eddiep.ghost.utils.PFunction;
import me.eddiep.ghost.utils.PRunnable;

import java.util.List;

public interface Physics {

    int addPhysicsEntity(PRunnable<Entity> onHit, Hitbox hitbox);

    int addPhysicsEntity(PRunnable<Entity> onHit, PRunnable<CollisionResult> onHit2, Hitbox hitbox);

    boolean checkEntity(Entity entity);

    boolean foreach(PFunction<Hitbox, Boolean> onHit);

    boolean removePhysicsEntity(int id);

    List<Hitbox> allHitboxes();
}