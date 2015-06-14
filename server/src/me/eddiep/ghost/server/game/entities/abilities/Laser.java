package me.eddiep.ghost.server.game.entities.abilities;

import me.eddiep.ghost.server.game.entities.LaserEntity;
import me.eddiep.ghost.server.game.entities.playable.Playable;
import me.eddiep.ghost.server.utils.TimeUtils;

import java.io.IOException;

public class Laser implements Ability<Playable> {
    private static final long STALL_TIME = 900L;
    private static final long ANIMATION_TIME = 350L;
    private static final long FADE_TIME = 500L;
    private Playable p;

    public Laser(Playable p) {
        this.p = p;
    }
    @Override
    public String name() {
        return "laser";
    }

    @Override
    public Playable owner() {
        return p;
    }

    @Override
    public void use(float targetX, float targetY, int action) {
        p.freeze(); //Freeze the player
        p.getEntity().setVelocity(0f, 0f);
        p.getEntity().setVisible(true);
        p.setCanFire(false);


        final LaserEntity laserEntity = new LaserEntity(p);
        laserEntity.setVisible(false);
        laserEntity.setPosition(p.getEntity().getPosition());
        laserEntity.setVelocity(0f, 0f);

        float x = p.getEntity().getX();
        float y = p.getEntity().getY();

        float asdx = targetX - x;
        float asdy = targetY - y;
        float inv = (float) Math.atan2(asdy, asdx);

        laserEntity.setRotation(inv);

        try {
            p.getMatch().spawnEntity(laserEntity);
        } catch (IOException e) {
            e.printStackTrace();
        }

        p.getEntity().shake(STALL_TIME);

        TimeUtils.executeIn(STALL_TIME, new Runnable() {
            @Override
            public void run() { //SHAKE
                laserEntity.setVisible(true); //Have the client animate it now
                laserEntity.startChecking();

                TimeUtils.executeIn(ANIMATION_TIME, new Runnable() {
                    @Override
                    public void run() {
                        laserEntity.fadeOut();

                        p.unfreeze();
                        p.onFire(); //Indicate this player is done firing
                        p.setCanFire(true);

                        TimeUtils.executeIn(FADE_TIME, new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    p.getMatch().despawnEntity(laserEntity);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
                });
            }
        });
    }
}
