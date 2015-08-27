package me.eddiep.ghost.game.match.abilities;

import me.eddiep.ghost.game.match.entities.PlayableEntity;
import me.eddiep.ghost.game.match.entities.ability.BoomerangEntity;
import me.eddiep.ghost.utils.Global;
import me.eddiep.ghost.utils.TimeUtils;

public class Boomerang implements Ability<PlayableEntity> {

    private static final long BASE_COOLDOWN = 315;
    private static final float BOOMERANG_SPEED = 10f;

    private PlayableEntity owner;
    private BoomerangEntity boomerang;

    private boolean active = false;
    private boolean returning = false;

    public ConditionalRunnable checker;

    private abstract class ConditionalRunnable implements Runnable {

        public boolean execute = true;

        public abstract void run();
    }

    public Boomerang(PlayableEntity owner) {
        this.owner = owner;
    }

    @Override
    public String name() {
        return "Boomerang";
    }

    @Override
    public PlayableEntity owner() {
        return owner;
    }

    @Override
    public void use(float targetX, float targetY, int actionRequested) {
        if (!active) {
            handleLaunch(targetX, targetY);
        } else {
            handleReturn(targetX, targetY);
        }
    }

    /**
     * Boomerang starts moving away.
     */
    public void handleLaunch(float targetX, float targetY) {
        owner.setVisible(true);
        owner.fadeOut(2000);
        owner.setCanFire(false);

        float x = owner.getX();
        float y = owner.getY();

        float dx = targetX - x;
        float dy = targetY - y;
        float inv = (float) Math.atan2(dy, dx);

        boomerang = new BoomerangEntity(owner);
        boomerang.setPosition(owner.getPosition().cloneVector());
        boomerang.setVelocity((float) Math.cos(inv) * BOOMERANG_SPEED, (float) Math.sin(inv) * BOOMERANG_SPEED);
        owner.getWorld().spawnEntity(boomerang);

        active = true;

        TimeUtils.executeInSync(500, new Runnable() {
            @Override
            public void run() {
                owner.setCanFire(true);
            }
        }, owner.getWorld());

        TimeUtils.executeInSync(2000, (checker = new ConditionalRunnable() {
            @Override
            public void run() {
                if (!returning && execute) {
                    handleReturn(Global.random(0, 1024), Global.random(0, 720));
                }
            }
        }), owner.getWorld());
    }

    /**
     * Boomerang starts coming back.
     */
    public void handleReturn(float x, float y) {
        boomerang.startReturn(x, y);
        returning = true;
    }

    /**
     * Boomerang came back, called by {@link BoomerangEntity}
     */
    public void onReturnFinished() {
        active = false;
        returning = false;
        checker.execute = false;

        long wait = owner.calculateFireRate(BASE_COOLDOWN); //Base value is 315ms
        TimeUtils.executeInSync(wait, new Runnable() {
            @Override
            public void run() {
                owner.setCanFire(true);
            }
        }, owner.getWorld());
    }
}
