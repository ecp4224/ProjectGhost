package me.eddiep.ghost.server.game.entities.abilities;

import me.eddiep.ghost.server.game.entities.CircleEntity;
import me.eddiep.ghost.server.game.entities.playable.Playable;
import me.eddiep.ghost.server.game.util.Vector2f;
import me.eddiep.ghost.server.utils.TimeUtils;

import java.io.IOException;

public class Circle implements Ability<Playable> {
    private static final long STALL = 1300L + 100L; //700 to appear and 100 small delay
    private static final float SPEED_DECREASE = 0.7f; //Increase the player's speed by 70%

    private Playable p;

    public Circle(Playable p) {
        this.p = p;
    }

    @Override
    public String name() {
        return "circle";
    }

    @Override
    public Playable owner() {
        return p;
    }

    @Override
    public void use(float targetX, float targetY, int actionRequested) {
        p.setCanFire(false);
        p.getEntity().setVisible(true);

        final float old_speed = p.getSpeed();
        p.setSpeed(p.getSpeed() * SPEED_DECREASE);

        final CircleEntity entity = new CircleEntity(p);
        entity.setPosition(new Vector2f(targetX, targetY));
        entity.setVisible(false);
        entity.setVelocity(0f, 0f);

        try {
            p.getMatch().spawnEntity(entity);
        } catch (IOException e) {
            e.printStackTrace();
        }

        TimeUtils.executeIn(STALL, new Runnable() {
            @Override
            public void run() {
                //This is a temp workaround until we get some kind of "ready to animate" packet
                //When the entity is set to visible, the client should start animating the circle
                entity.setVisible(true); //Have the client animate it now
                try {
                    entity.updateState();
                } catch (IOException e) {
                    e.printStackTrace();
                }

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
