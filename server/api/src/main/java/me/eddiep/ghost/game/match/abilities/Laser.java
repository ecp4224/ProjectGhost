package me.eddiep.ghost.game.match.abilities;

import me.eddiep.ghost.game.match.entities.PlayableEntity;
import me.eddiep.ghost.game.match.world.ParticleEffect;
import me.eddiep.ghost.utils.*;

public class Laser implements Ability<PlayableEntity> {
    private static final long STALL_TIME = 600L;
    private static final long ANIMATION_TIME = 350L;
    private static final long FADE_TIME = 500L;
    private static final long BASE_COOLDOWN = 315;
    private PlayableEntity p;

    public Laser(PlayableEntity p) {
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


        /*final LaserEntity laserEntity = new LaserEntity(p);
        laserEntity.setVisible(false);
        laserEntity.setPosition(p.getPosition());
        laserEntity.setVelocity(0f, 0f);*/

        float x = p.getX();
        float y = p.getY();

        float asdx = targetX - x;
        float asdy = targetY - y;
        final float inv = (float) Math.atan2(asdy, asdx);

        //laserEntity.setRotation(inv);

        //p.getWorld().spawnEntity(laserEntity);

        float sx = p.getX(), sy = p.getY() + 20f;
        float bx = p.getX() + 1040;
        float by = p.getY() - 20f;

        //Center of rotation
        final Vector2f[] hitbox = VectorUtils.rotatePoints(inv, p.getPosition(),
                new Vector2f(sx, sy),
                new Vector2f(bx, sy),
                new Vector2f(bx, by),
                new Vector2f(sx, by)
        );

        /*
        1. Check all hitboxes for intersection of all faces of the hitbox and get the closet
        2. Get normal of hitbox
        3. Calculate reflection vector of laser hitbox
        4. Find point of intersection and use that as new length
        5. Take remainder as the second half and create second hitbox
        6. Rotate second hitbox with same angle as reflected vector
        7. ????
        8. Profit
         */

        p.getWorld().spawnParticle(ParticleEffect.CHARGE, (int)STALL_TIME, 64, p.getX(), p.getY(), inv);
        p.shake(STALL_TIME);

        TimeUtils.executeInSync(STALL_TIME, new Runnable() {
            @Override
            public void run() { //SHAKE
                p.getWorld().spawnParticle(ParticleEffect.LINE, 500, 20, p.getX(), p.getY(), inv);
                p.getWorld().requestEntityUpdate();

                final HitboxHelper.HitboxToken helper = HitboxHelper.checkHitboxEveryTick(hitbox, p);

                TimeUtils.executeInSync(ANIMATION_TIME, new Runnable() {
                    @Override
                    public void run() {
                        //laserEntity.fadeOut(500);

                        p.unfreeze();
                        p.onFire(); //Indicate this player is done firing

                        TimeUtils.executeInSync(FADE_TIME, new Runnable() {
                            @Override
                            public void run() {
                                helper.stopChecking();

                                long wait = p.calculateFireRate(BASE_COOLDOWN);
                                try {
                                    Thread.sleep(wait);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                p.setCanFire(true);
                            }
                        }, p.getWorld());
                    }
                }, p.getWorld());
            }
        }, p.getWorld());
    }
}
