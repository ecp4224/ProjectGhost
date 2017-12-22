package com.boxtrotstudio.ghost.game.match.abilities;

import com.boxtrotstudio.ghost.game.match.Event;
import com.boxtrotstudio.ghost.game.match.entities.PlayableEntity;
import com.boxtrotstudio.ghost.game.match.entities.ability.BoomerangEntity;
import com.boxtrotstudio.ghost.utils.TimeUtils;
import com.boxtrotstudio.ghost.utils.Vector2f;

public class Boomerang implements Ability<PlayableEntity> {

    private static final long BASE_COOLDOWN = 315;
    private static final float BOOMERANG_SPEED = 10f;
    private static final long DEFAULT_RETURN_TIME = 1000;

    private PlayableEntity owner;
    private BoomerangEntity boomerang;

    private boolean active = false;
    private boolean returning = false;
    private boolean canFire = false;

    public ConditionalRunnable checker;

    private abstract class ConditionalRunnable implements Runnable {

        public boolean execute = true;

        public abstract void run();
    }

    public Boomerang(PlayableEntity owner) {
        this.owner = owner;
    }

    public Boomerang() { }

    @Override
    public String name() {
        return "Boomerang";
    }

    @Override
    public PlayableEntity owner() {
        return owner;
    }

    @Override
    public void usePrimary(float targetX, float targetY) {
        if (!active) {
            handleLaunch(targetX, targetY);
        } else {
            handleReturn(targetX, targetY);
        }
    }

    @Override
    public void useSecondary(float targetX, float targetY) {

    }

    @Override
    public byte id() {
        return 4;
    }

    @Override
    public boolean canFirePrimary() {
        return canFire;
    }

    @Override
    public boolean canFireSecondary() {
        return false;
    }

    /**
     * Boomerang starts moving away.
     */
    public void handleLaunch(float targetX, float targetY) {
        if (!canFire)
            return;

        owner.setVisible(true);
        canFire = false;

        float x = owner.getX();
        float y = owner.getY();

        float dx = targetX - x;
        float dy = targetY - y;
        double direction = Math.atan2(dy, dx);
        owner.triggerEvent(Event.FireBoomerang, direction);
        float inv = (float) direction;

        float acceleration_speed = -(BOOMERANG_SPEED / 100f);
        Vector2f acceleration = new Vector2f((float)Math.cos(inv) * acceleration_speed, (float)Math.sin(inv) * acceleration_speed);

        boomerang = new BoomerangEntity(owner, acceleration);
        boomerang.setPosition(owner.getPosition().cloneVector());
        boomerang.setVelocity((float) Math.cos(inv) * BOOMERANG_SPEED, (float) Math.sin(inv) * BOOMERANG_SPEED);
        owner.getWorld().spawnEntity(boomerang);

        active = true;
        owner.onFire(); //Indicate the player has fired, also triggers the fade out

        TimeUtils.executeInSync(200, () -> canFire = true, owner.getWorld());

        TimeUtils.executeInSync(DEFAULT_RETURN_TIME, (checker = new ConditionalRunnable() {
            @Override
            public void run() {
                if (!returning && execute) {
                    float x = owner.getX();
                    float y = owner.getY();
                    if (owner.hasTarget()) {
                        x = owner.getTarget().x;
                        y = owner.getTarget().y;
                    }

                    canFire = false; //The player can't fire while the boomerang is returning

                    boomerang.startReturn(x, y);
                    returning = true;
                }
            }
        }), owner.getWorld());
    }

    /**
     * Boomerang starts coming back.
     */
    public void handleReturn(float x, float y) {
        owner.setVisible(true);
        canFire = false; //The player can't fire while the boomerang is returning

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
        TimeUtils.executeInSync(wait, () -> canFire = true, owner.getWorld());
    }
}
