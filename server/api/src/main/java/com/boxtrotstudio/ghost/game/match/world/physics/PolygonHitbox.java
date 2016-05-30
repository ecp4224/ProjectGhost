package com.boxtrotstudio.ghost.game.match.world.physics;

import com.boxtrotstudio.ghost.utils.Vector2f;
import com.boxtrotstudio.ghost.utils.VectorUtils;

public class PolygonHitbox implements Hitbox {
    private Polygon bounds;
    private double rotation;
    private String name;

    public PolygonHitbox(String name, Polygon bounds) {
        this.bounds = bounds;
        this.name = name;
    }

    public PolygonHitbox(String name, Vector2f... points) {
        this.bounds = new Polygon(points);
        this.name = name;
    }

    @Override
    public Polygon getPolygon() {
        return bounds;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean isPointInside(Vector2f point) {
        return VectorUtils.isPointInside(point, bounds.getPoints());
    }

    @Override
    public CollisionResult isHitboxInside(Hitbox hitbox) {
        if (!hitbox.hasPolygon()) {
            //This hitbox doesn't have a polygon
            return hitbox.isHitboxInside(this);
        }

        for (Vector2f point : hitbox.getPolygon().getPoints()) {
            if (VectorUtils.isPointInside(point, bounds.getPoints()))
                return new CollisionResult(true, point);
        }

        Polygon polygon = hitbox.getPolygon();

        for (Face face : this.bounds.getFaces()) {
            for (Face otherFace : polygon.getFaces()) {
                Vector2f pointOfIntersection = VectorUtils.pointOfIntersection(
                        face.getPointA(), face.getPointB(),
                        otherFace.getPointA(), otherFace.getPointB()
                );
                if (pointOfIntersection == null)
                    continue;
                return new CollisionResult(true, pointOfIntersection);
            }
        }
        return CollisionResult.NO_HIT;
    }

    @Override
    public boolean hasPolygon() {
        return true;
    }

    public static PolygonHitbox createCircleHitbox(double radius, int resolution, String name) {
        int iterations = 360 / resolution;
        Vector2f[] points = new Vector2f[iterations];

        for (int i = 0; i < iterations; i++) {
            double val = Math.toRadians(i * (360 / iterations));

            double x = Math.cos(val) * radius;
            double y = Math.sin(val) * radius;

            points[i] = new Vector2f((float)x, (float)y);
        }

        return new PolygonHitbox(name, points);
    }
}
