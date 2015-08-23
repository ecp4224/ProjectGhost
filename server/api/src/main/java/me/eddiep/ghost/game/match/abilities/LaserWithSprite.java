package me.eddiep.ghost.game.match.abilities;

import me.eddiep.ghost.game.match.entities.PlayableEntity;
import me.eddiep.ghost.game.match.entities.ability.LaserEntity;
import me.eddiep.ghost.game.match.world.ParticleEffect;
import me.eddiep.ghost.utils.TimeUtils;

public class LaserWithSprite implements Ability<PlayableEntity> {
    private static final long STALL_TIME = 600L;
    private static final long ANIMATION_TIME = 350L;
    private static final long FADE_TIME = 500L;
    private static final long BASE_COOLDOWN = 315;
    private PlayableEntity p;

    public LaserWithSprite(PlayableEntity p) {
        this.p = p;
    }
    @Override
    public String name() {
        return "laser";
    }

    @Override
    public PlayableEntity owner() {
        return p;
    }

    @Override
    public void use(float targetX, float targetY, int action) {
        p.freeze(); //Freeze the player
        p.setVelocity(0f, 0f);
        p.setVisible(true);
        p.setCanFire(false);


        final LaserEntity laserEntity = new LaserEntity(p);
        laserEntity.setVisible(false);
        laserEntity.setPosition(p.getPosition());
        laserEntity.setVelocity(0f, 0f);

        float x = p.getX();
        float y = p.getY();

        float asdx = targetX - x;
        float asdy = targetY - y;
        float inv = (float) Math.atan2(asdy, asdx);

        laserEntity.setRotation(inv);

        p.getWorld().spawnEntity(laserEntity);

        p.getWorld().spawnParticle(ParticleEffect.CHARGE, (int)STALL_TIME, 64, p.getX(), p.getY(), inv);
        p.shake(STALL_TIME);

        TimeUtils.executeIn(STALL_TIME, new Runnable() {
            @Override
            public void run() { //SHAKE
                //This is a temp workaround until we get some kind of "ready to animate" packet
                //When the entity is set to visible, the client should start animating the laser
                laserEntity.setVisible(true); //Have the client animate it now
                p.getWorld().requestEntityUpdate();

                laserEntity.startChecking(); //Start checking for collision

                TimeUtils.executeIn(ANIMATION_TIME, new Runnable() {
                    @Override
                    public void run() {
                        laserEntity.fadeOut(500);

                        p.unfreeze();
                        p.onFire(); //Indicate this player is done firing

                        TimeUtils.executeIn(FADE_TIME, new Runnable() {
                            @Override
                            public void run() {
                                p.getWorld().despawnEntity(laserEntity);

                                long wait = p.calculateFireRate(BASE_COOLDOWN);
                                try {
                                    Thread.sleep(300);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                p.setCanFire(true);
                            }
                        });
                    }
                });
            }
        });
    }
}
