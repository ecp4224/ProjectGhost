package me.eddiep.ghost.game.match.entities.ability;

import me.eddiep.ghost.game.match.Event;
import me.eddiep.ghost.game.match.abilities.Boomerang;
import me.eddiep.ghost.game.match.entities.BaseEntity;
import me.eddiep.ghost.game.match.entities.PlayableEntity;
import me.eddiep.ghost.game.match.entities.TypeableEntity;
import me.eddiep.ghost.game.match.stats.BuffType;
import me.eddiep.ghost.game.match.world.physics.PhysicsEntity;
import me.eddiep.ghost.utils.Vector2f;

import java.util.ArrayList;

public class BoomerangEntity extends BaseEntity implements TypeableEntity {
    private static final double STEP = 5 * Math.PI / 180;

    private PlayableEntity parent;
    private double rotation = 0;

    private ArrayList<PlayableEntity> alreadyHit = new ArrayList<>(); //Players already hit
    private Vector2f acceleration; //Force the boomerang to go in the other direction
    private boolean returning;
    private BoomerangLineEntity line;

    public BoomerangEntity(PlayableEntity parent, Vector2f acceleration) {
        super();
        setParent(parent);
        setMatch(parent.getMatch());
        setVisible(true);
        setName("BOOMERANG");
        this.parent = parent;
        this.acceleration = acceleration;
    }

    @Override
    public void tick() {
        setRotation(rotation += STEP);

        PlayableEntity[] opponents = parent.getOpponents();
        for (PlayableEntity toHit : opponents) {
            if (toHit.isDead() || alreadyHit.contains(toHit))
                continue;

            if (isInside(toHit.getX() - 24f,
                    toHit.getY() - 24f,
                    toHit.getX() + 24f,
                    toHit.getY() + 24f)) {

                alreadyHit.add(toHit);
                toHit.subtractLife();
                if (!toHit.isVisible()) {
                    toHit.setVisible(true);
                }

                toHit.onDamage(parent); //p was damaged by the parent

                parent.onDamagePlayable(toHit); //the parent damaged p
                if (toHit.isDead()) {
                    parent.onKilledPlayable(toHit);
                }
            }
        }

        position.x += velocity.x;
        position.y += velocity.y;

        velocity.x += acceleration.x;
        velocity.y += acceleration.y;

        if (returning) {
            Vector2f lower = containingMatch.getLowerBounds();
            Vector2f upper = containingMatch.getUpperBounds();

            if (isInside(parent.getX() - 24f,
                    parent.getY() - 24f,
                    parent.getX() + 24f,
                    parent.getY() + 24f)) {
                finishReturn();
                parent.getSpeedStat().addTimedBuff("Boomerang Catch", BuffType.PercentAddition, 10.0, 3.0);
                parent.triggerEvent(Event.BoomerangCatch, 0); //no direction
            } else if (position.x < lower.x ||
                    position.x > upper.x ||
                    position.y < lower.y ||
                    position.y > upper.y) {
                finishReturn();
            }
        }

        super.tick();
    }

    @Override
    public short getType() {
        return 5;
    }

    /**
     * Starts coming back.
     */
    public void startReturn(float controlX, float controlY) {
        double inv = Math.atan2(controlY - position.y, controlX - position.x);

        line = new BoomerangLineEntity();
        line.setPosition(position.cloneVector());
        line.setRotation(inv);
        world.spawnEntityFor(parent, line);


        float acceleration_speed = (10f / 100f);
        this.velocity = new Vector2f(0f, 0f);
        this.acceleration = new Vector2f((float)Math.cos(inv) * acceleration_speed, (float)Math.sin(inv) * acceleration_speed);
        alreadyHit.clear();

        returning = true;

        parent.setCanFire(false);
    }

    /**
     * Marks current throw as done, allowing the player to throw again.
     */
    private void finishReturn() {
        if (line != null) {
            world.despawnEntityFor(parent, line);
        }

        world.despawnEntity(this);

        ((Boomerang) parent.currentAbility()).onReturnFinished();
    }

    @Override
    public void onCollision(PhysicsEntity entity) {
        if (line != null) {
            world.despawnEntityFor(parent, line);
        }

        super.onCollision(entity);
        ((Boomerang) parent.currentAbility()).onReturnFinished();
    }
}
