package me.eddiep.ghost.client.core.physics;

import me.eddiep.ghost.client.core.render.Drawable;

public interface PhysicsEntity extends Drawable {

    Hitbox getHitbox();
}
