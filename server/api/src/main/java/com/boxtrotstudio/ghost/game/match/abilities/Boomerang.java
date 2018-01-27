package com.boxtrotstudio.ghost.game.match.abilities;

import com.boxtrotstudio.ghost.game.match.Event;
import com.boxtrotstudio.ghost.game.match.entities.PlayableEntity;
import com.boxtrotstudio.ghost.game.match.entities.ability.BoomerangEntity;
import com.boxtrotstudio.ghost.utils.TimeUtils;
import com.boxtrotstudio.ghost.utils.Vector2f;

public class Boomerang extends PlayerAbility {

    private static final long BASE_COOLDOWN = 315;
    private static final float BOOMERANG_SPEED = 10f;
    private static final long DEFAULT_RETURN_TIME = 1000;

    private BoomerangEntity boomerang;

    private boolean active = false;
    private boolean returning = false;

    public ConditionalRunnable checker;

    private abstract class ConditionalRunnable implements Runnable {

        public boolean execute = true;

        public abstract void run();
    }

    public Boomerang(PlayableEntity owner) {
        super(owner);
        baseCooldown = BASE_COOLDOWN;
        canCancel = false;
    }

    @Override
    public String name() {
        return "Boomerang";
    }

    @Override
    protected void onUsePrimary(float targetX, float targetY) {
        if (active)
            return;

        PlayableEntity owner = owner();

        owner.setVisible(true);

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

        //Setup ability to use secondary weapon
        TimeUtils.executeInSync(200, () -> canUseSecondary = true, owner.getWorld());

        executeInSync(DEFAULT_RETURN_TIME, (checker = new ConditionalRunnable() {
            @Override
            public void run() {
                if (!returning && execute) {
                    float x = owner.getX();
                    float y = owner.getY();
                    if (owner.hasTarget()) {
                        x = owner.getTarget().x;
                        y = owner.getTarget().y;
                    }

                    canUseSecondary = false;
                    canUsePrimary = false; //The player can't fire while the boomerang is returning

                    boomerang.startReturn(x, y);
                    returning = true;
                }
            }
        }));
    }

    @Override
    protected void onUseSecondary(float targetX, float targetY) {
        if (active) {
            handleReturn(targetX, targetY);
        }
    }

    @Override
    public byte id() {
        return 4;
    }

    /**
     * Boomerang starts coming back.
     */
    public void handleReturn(float x, float y) {
        PlayableEntity owner = owner();
        owner.setVisible(true);

        canUseSecondary = false;
        canUsePrimary = false; //The player can't fire while the boomerang is returning

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

        endPrimary();
    }
}
