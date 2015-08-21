package me.eddiep.ghost.game.match.world.physics;

import me.eddiep.ghost.game.match.entities.Entity;
import me.eddiep.ghost.utils.PRunnable;
import me.eddiep.ghost.utils.Vector2f;

public interface Physics {

    int addPhysicsEntity(PRunnable<Entity> onHit, Vector2f... hitbox);

    void checkEntity(Entity entity);

    void checkEntity(PhysicsEntity entity);

    boolean removePhysicsEntity(int id);
}
