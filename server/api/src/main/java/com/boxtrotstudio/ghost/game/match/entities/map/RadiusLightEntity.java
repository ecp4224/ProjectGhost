package com.boxtrotstudio.ghost.game.match.entities.map;

import com.boxtrotstudio.ghost.game.match.entities.Entity;
import com.boxtrotstudio.ghost.utils.Vector2f;

public class RadiusLightEntity extends BaseLightEntity {
    @Override
    public Vector2f[] generateHitboxPoints() {
        final float radius = width;
        final int iterations = 72;
        Vector2f[] points = new Vector2f[iterations];

        for (int i = 0; i < iterations; i++) {
            double val = Math.toRadians(i * (360 / iterations));

            double x = Math.cos(val) * radius;
            double y = Math.sin(val) * radius;

            points[i] = new Vector2f((float)x, (float)y);
        }

        return points;
    }

    @Override
    public void onHit(Entity entity) {

    }

    @Override
    public short getType() {
        return 86;
    }
}
