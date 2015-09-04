package me.eddiep.ghost.game.match.world.physics;

import me.eddiep.ghost.utils.Vector2f;

public interface Hitbox {
    boolean isPointInside(Vector2f point);

    boolean isHitboxInside(Hitbox hitbox);

    boolean hasPolygon();

    Polygon getPolygon();

    String getName();
}
