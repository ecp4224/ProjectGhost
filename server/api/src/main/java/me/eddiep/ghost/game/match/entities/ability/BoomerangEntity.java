package me.eddiep.ghost.game.match.entities.ability;

import me.eddiep.ghost.game.match.abilities.Boomerang;
import me.eddiep.ghost.game.match.entities.BaseEntity;
import me.eddiep.ghost.game.match.entities.PlayableEntity;
import me.eddiep.ghost.game.match.entities.TypeableEntity;
import me.eddiep.ghost.utils.Vector2f;

public class BoomerangEntity extends BaseEntity implements TypeableEntity {

    private static final float SPEED = 16;
    private static final double STEP = 5 * Math.PI / 180;

    private PlayableEntity parent;
    private double rotation = 0;

    private boolean returning = false;
    private float t = 0.05f;   //movement step
    private Vector2f control;  //control point position
    private Vector2f owner;    //player position
    private Vector2f self;     //boomerang position

    private boolean recalculate = true;
    private float px;
    private float py;

    public BoomerangEntity(PlayableEntity parent) {
        super();
        setParent(parent);
        setMatch(parent.getMatch());
        setVisible(true);
        setName("BOOMERANG");
        this.parent = parent;
    }

    @Override
    public void tick() {
        setRotation(rotation += STEP);

        PlayableEntity[] opponents = parent.getOpponents();
        for (PlayableEntity toHit : opponents) {
            if (toHit.isDead())
                continue;

            if (isInside(toHit.getX() - 24f,
                    toHit.getY() - 24f,
                    toHit.getX() + 24f,
                    toHit.getY() + 24f)) {

                toHit.subtractLife();
                if (!toHit.isVisible()) {
                    toHit.setVisible(true);
                }

                toHit.onDamage(parent); //p was damaged by the parent

                finishReturn();
                parent.onDamagePlayable(toHit); //the parent damaged p
                if (toHit.isDead()) {
                    parent.onKilledPlayable(toHit);
                }
            }
        }

        if (returning && recalculate) {
            float omtsq = (1 - t) * (1 - t);
            float tsq = t * t;

            px = omtsq * self.x + 2 * (1 - t) * t * control.x + tsq * owner.x;
            py = omtsq * self.y + 2 * (1 - t) * t * control.y + tsq * owner.y;

            float dx = px - position.x;
            float dy = py - position.y;
            float inv = (float) Math.atan2(dy, dx);

            setVelocity((float) Math.cos(inv) * SPEED, (float) Math.sin(inv) * SPEED);
            recalculate = false;

            t += 0.05;

            if (t > 1) {
                finishReturn();
            }
        }

        position.x += velocity.x;
        position.y += velocity.y;

        if (Math.abs(position.x - px) <= SPEED && Math.abs(position.y - py) <= SPEED) {
            recalculate = true;
        }
    }

    @Override
    public byte getType() {
        return 5;
    }

    /**
     * Starts coming back.
     */
    public void startReturn(float controlX, float controlY) {
        control = new Vector2f(controlX, controlY);
        owner = parent.getPosition().cloneVector();
        self = getPosition().cloneVector();
        returning = true;

        parent.setCanFire(false);
    }

    /**
     * Marks current throw as done, allowing the player to throw again.
     */
    private void finishReturn() {
        world.despawnEntity(this);
        ((Boomerang) parent.currentAbility()).onReturnFinished();
    }
}
