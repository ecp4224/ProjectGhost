package me.eddiep.ghost.game.match.abilities;

import me.eddiep.ghost.game.match.entities.PlayableEntity;
import me.eddiep.ghost.game.match.entities.impl.CircleEntity;
import me.eddiep.ghost.utils.TimeUtils;
import me.eddiep.ghost.utils.Vector2f;

public class Circle implements Ability<PlayableEntity> {
    private static final long STALL = 550L + 100L; //700 to appear and 100 small delay
    private static final float SPEED_DECREASE = 0.7f; //Increase the player's speed by 70%

    private PlayableEntity p;

    public Circle(PlayableEntity p) {
        this.p = p;
    }

    @Override
    public String name() {
        return "circle";
    }

    @Override
    public PlayableEntity owner() {
        return p;
    }

    @Override
    public void use(float targetX, float targetY, int actionRequested) {
        p.setCanFire(false);
        p.setVisible(true);

        final float old_speed = p.getSpeed();
        p.setSpeed(p.getSpeed() * SPEED_DECREASE);

        final CircleEntity entity = new CircleEntity(p);
        entity.setPosition(new Vector2f(targetX, targetY));
        entity.setVisible(false);
        entity.setVelocity(0f, 0f);

        p.getMatch().getWorld().spawnEntity(entity);

        TimeUtils.executeIn(STALL, new Runnable() {
            @Override
            public void run() {
                //This is a temp workaround until we get some kind of "ready to animate" packet
                //When the entity is set to visible, the client should start animating the circle
                entity.setVisible(true); //Have the client animate it now
                p.getWorld().requestEntityUpdate();

                entity.checkDamage();

                TimeUtils.executeIn(1300, new Runnable() {
                    @Override
                    public void run() {
                        p.setSpeed(old_speed);

                        p.onFire(); //Indicate this player is done firing
                        p.setCanFire(true);

                        entity.fadeOutAndDespawn(500);
                    }
                });
            }
        });
    }
}
