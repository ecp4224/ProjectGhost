package me.eddiep.ghost.game.match.world.physics;

import me.eddiep.ghost.game.match.entities.TypeableEntity;
import me.eddiep.ghost.utils.Vector2f;

public interface PhysicsEntity extends TypeableEntity {

    Vector2f[] getHitbox();
}
