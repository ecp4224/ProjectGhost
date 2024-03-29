package com.boxtrotstudio.ghost.game.match.entities.ability;

import com.boxtrotstudio.ghost.game.match.entities.BaseEntity;
import com.boxtrotstudio.ghost.game.match.entities.PlayableEntity;
import com.boxtrotstudio.ghost.game.match.entities.TypeableEntity;
import com.boxtrotstudio.ghost.utils.Vector2f;
import com.boxtrotstudio.ghost.utils.VectorUtils;

import java.util.ArrayList;

public class LaserEntity extends BaseEntity implements TypeableEntity {
    private PlayableEntity parent;
    private ArrayList<PlayableEntity> alreadyHit = new ArrayList<>();
    public LaserEntity(PlayableEntity parent) {
        super();
        setParent(parent);
        setMatch(parent.getMatch());
        setVisible(true);
        setName("LASERS");
        this.parent = parent;
    }

    @Override
    public void tick() {
        if (check) {
            //float currentWidth = TimeUtils.ease(0f, 1040f, 300f, System.currentTimeMillis() - start);

            float x = getX(), y = getY() + 20f;
            float bx = parent.getX() + 1040;
            float by = parent.getY() - 20f;

                                                               //Center of rotation
            Vector2f[] rect = VectorUtils.rotatePoints(rotation, getPosition(),
                    new Vector2f(x, y),
                    new Vector2f(bx, y),
                    new Vector2f(bx, by),
                    new Vector2f(x, by)
            );

            PlayableEntity[] opponents = parent.getOpponents();
            for (PlayableEntity p : opponents) {
                if (alreadyHit.contains(p))
                    continue;
                if (p.isDead())
                    continue;

                if (VectorUtils.isPointInside(p.getPosition(), rect)) {
                    p.subtractLife();
                    if (!p.isVisible()) {
                        p.setVisible(true);
                    }

                    p.onDamage(parent); //p was damaged by the parent

                    parent.onDamagePlayable(p); //the parent damaged p
                    if (p.isDead()) {
                        parent.onKilledPlayable(p);
                    }

                    alreadyHit.add(p);
                }
            }
        }

        super.tick();
    }

    @Override
    public short getType() {
        return 3;
    }

    private boolean check;
    private long start;
    public void startChecking() {
        check = true;
        start = System.currentTimeMillis();
    }
}
