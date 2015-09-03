package me.eddiep.ghost.game.match.world.physics;

import me.eddiep.ghost.utils.Vector2f;
import me.eddiep.ghost.utils.VectorUtils;

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
