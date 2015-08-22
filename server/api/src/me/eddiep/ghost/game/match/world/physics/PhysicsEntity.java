package me.eddiep.ghost.game.match.world.physics;

import me.eddiep.ghost.game.match.entities.TypeableEntity;

public interface PhysicsEntity extends TypeableEntity {

    Hitbox getHitbox();
}
