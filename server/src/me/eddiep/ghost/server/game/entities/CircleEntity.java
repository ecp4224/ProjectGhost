package me.eddiep.ghost.server.game.entities;

import me.eddiep.ghost.server.game.Entity;
import me.eddiep.ghost.server.game.entities.playable.Playable;
import me.eddiep.ghost.server.game.entities.playable.impl.Player;
import me.eddiep.ghost.server.game.util.Vector2f;
import me.eddiep.ghost.server.utils.MathUtils;

import java.io.IOException;
import java.util.ArrayList;

public class CircleEntity extends Entity implements TypeableEntity {
    private static final float RADIUS = 128f;

    private Playable parent;
    private ArrayList<Playable> alreadyHit = new ArrayList<>();
    private Vector2f[] points = new Vector2f[20];
    public CircleEntity(Playable parent) {
        super();
        setParent(parent.getEntity());
        setMatch(parent.getMatch());
        setVisible(true);
        setName("CIRCLES");
        this.parent = parent;
    }

    @Override
    public void updateState() throws IOException {
        Playable[] temp = parent.getOpponents();
        for (Playable p : temp) {
            updateStateFor(p);
        }

        temp = parent.getAllies();
        for (Playable p : temp) {
            updateStateFor(p);
        }
    }

    @Override
    public byte getType() {
        return 4;
    }

    private boolean check;
    public void checkDamage() {
        check = true;

        float ox = getX() + RADIUS;
        float oy = getY() + RADIUS;
        for (int i = 0; i < 20; i++) {
            float x = (float) (ox * Math.cos(Math.toRadians(i * 18)));
            float y = (float) (oy * Math.sin(Math.toRadians(i * 18)));

            points[i] = new Vector2f(x, y);
        }

        Playable[] opponents = parent.getOpponents();
        for (Playable p : opponents) {
            Entity toHit = p.getEntity();
            if (MathUtils.isPointInside(toHit.getPosition(), points)) {

                p.subtractLife();
                if (!toHit.isVisible()) {
                    toHit.setVisible(true);
                }

                p.onDamage(parent); //p was damaged by the parent

                parent.onDamagePlayable(p); //the parent damaged p
                if (p.isDead()) {
                    parent.onKilledPlayable(p);
                }
            }
        }
    }
}
