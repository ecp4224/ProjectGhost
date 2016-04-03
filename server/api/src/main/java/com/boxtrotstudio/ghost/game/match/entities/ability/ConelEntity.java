package com.boxtrotstudio.ghost.game.match.entities.ability;

import com.boxtrotstudio.ghost.game.match.world.physics.PhysicsEntity;
import com.boxtrotstudio.ghost.game.match.world.physics.PolygonHitbox;
import com.boxtrotstudio.ghost.utils.Vector2f;
import com.boxtrotstudio.ghost.game.match.entities.BaseEntity;
import com.boxtrotstudio.ghost.game.match.entities.PlayableEntity;
import com.boxtrotstudio.ghost.game.match.entities.TypeableEntity;

/*
This entity will be setup so the center of the entity
is the start of the cone.
 */
public class ConelEntity extends BaseEntity implements TypeableEntity {
    private static final float WIDTH = 32, HEIGHT = 32;

    private PlayableEntity parent;
    public ConelEntity(PlayableEntity parent, float direction) {
        super();
        this.parent = parent;
        setParent(parent);
        setMatch(parent.getMatch());
        setVisible(true);
        setName("BOOMERANG");
        setRotation(direction);
        checkPhysics(false); //This entity does not need to check physics
    }

    @Override
    public void tick() {
        super.tick();

        setPosition(parent.getPosition());
    }

    @Override
    public short getType() {
        return 0;
    }

    @Override
    public void onCollision(PhysicsEntity entity) { } //Do nothing

    public void performCheck() {
        Vector2f[] points = new Vector2f[] {
                new Vector2f(position.x, position.y),
                new Vector2f(position.x + (WIDTH / 2f), position.y + (HEIGHT / 2f)),
                new Vector2f(position.x + (WIDTH / 2f), position.y - (HEIGHT / 2f))
        };
        PolygonHitbox tempHitbox = new PolygonHitbox("CONELAOE", points);

        PlayableEntity[] opponents = parent.getOpponents();
        for (PlayableEntity p : opponents) {
            if (p.isDead())
                continue;

            if (tempHitbox.isHitboxInside(p.getHitbox()).didHit()) {
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
}
