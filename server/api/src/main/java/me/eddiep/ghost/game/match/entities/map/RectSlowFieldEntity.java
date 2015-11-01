package me.eddiep.ghost.game.match.entities.map;

import me.eddiep.ghost.utils.Vector2f;

public class RectSlowFieldEntity extends BaseSlowFieldEntity {
    @Override
    public Vector2f[] generateHitboxPoints() {
        float x1 = getX() - (width / 2f), x2 = getX() + (width / 2f);
        float y1 = getY() - (height / 2f), y2 = getY() + (height / 2f);

        return new Vector2f[] {
                new Vector2f(x1, y1),
                new Vector2f(x1, y2),
                new Vector2f(x2, y2),
                new Vector2f(x2, y1)
        };
    }

    @Override
    public short getType() {
        return 84;
    }
}
