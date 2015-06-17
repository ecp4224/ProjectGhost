package me.eddiep.ghost.server.game.entities;

import me.eddiep.ghost.server.game.BaseEntity;
import me.eddiep.ghost.server.game.util.Vector2f;
import me.eddiep.ghost.server.utils.MathUtils;

import java.util.ArrayList;

public class CircleEntity extends BaseEntity implements TypeableEntity {
    private static final float RADIUS = 128f / 2f;

    private PlayableEntity parent;
    private ArrayList<PlayableEntity> alreadyHit = new ArrayList<>();
    private Vector2f[] points = new Vector2f[20];
    public CircleEntity(PlayableEntity parent) {
        super();
        setParent(parent);
        setMatch(parent.getMatch());
        setVisible(true);
        setName("CIRCLES");
        this.parent = parent;
    }

    @Override
    public byte getType() {
        return 4;
    }

    private boolean check;
    public void checkDamage() {
        check = true;

        float cx = getX();
        float cy = getY();

        for (int i = 0; i < 20; i++) {
            float x = (float) (cx + RADIUS * Math.cos(Math.toRadians(i * 18)));
            float y = (float) (cy + RADIUS * Math.sin(Math.toRadians(i * 18)));

            points[i] = new Vector2f(x, y);
        }

        PlayableEntity[] opponents = parent.getOpponents();
        for (PlayableEntity p : opponents) {
            if (MathUtils.isPointInside(p.getPosition(), points)) {

                p.subtractLife();
                if (!p.isVisible()) {
                    p.setVisible(true);
                }

                p.onDamage(parent); //p was damaged by the parent

                parent.onDamagePlayable(p); //the parent damaged p
                if (p.isDead()) {
                    parent.onKilledPlayable(p);
                }
            }
        }
    }

    @Override
    public void tick() { }
}
