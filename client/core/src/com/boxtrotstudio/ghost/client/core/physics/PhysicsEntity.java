package com.boxtrotstudio.ghost.client.core.physics;

import com.boxtrotstudio.ghost.client.core.render.Drawable;

public interface PhysicsEntity extends Drawable {

    Hitbox getHitbox();
}
