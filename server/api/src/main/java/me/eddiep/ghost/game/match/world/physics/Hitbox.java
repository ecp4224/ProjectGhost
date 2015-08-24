package me.eddiep.ghost.game.match.world.physics;

import me.eddiep.ghost.utils.Vector2f;
import me.eddiep.ghost.utils.VectorUtils;

public class Hitbox {
    private Polygon bounds;
    private double rotation;
    private String name;

    public Hitbox(String name, Polygon bounds) {
        this.bounds = bounds;
        this.name = name;
    }

    public Hitbox(String name, Vector2f... points) {
        this.bounds = new Polygon(points);
        this.name = name;
    }

    public Polygon getPolygon() {
        return bounds;
    }

    public String getName() {
        return name;
    }

    public boolean isPointInside(Vector2f point) {
        return VectorUtils.isPointInside(point, bounds.getPoints());
    }

    public boolean isHitboxInside(Hitbox hitbox) {
        for (Face face : hitbox.getPolygon().getFaces()) {
            for (Face face2 : bounds.getFaces()) {
                boolean isIntersecting = VectorUtils.lineIntersects(face.getPointA(), face.getPointB(), face2.getPointA(), face2.getPointB());
                if (isIntersecting)
                    return true;
            }
        }

        return false;
    }
}
