package com.boxtrotstudio.ghost.game.match.abilities;

import com.boxtrotstudio.ghost.game.match.entities.PlayableEntity;
import com.boxtrotstudio.ghost.utils.TimeUtils;

public abstract class PlayerAbility implements Ability<PlayableEntity> {
    private final PlayableEntity p;
    protected boolean runningPrimary, runningSecondary;
    protected boolean canUsePrimary = true, canUseSecondary = true;
    protected boolean canCancel = true;
    protected long baseCooldown = 555;

    public PlayerAbility(PlayableEntity owner) {
        this.p = owner;
    }

    @Override
    public PlayableEntity owner() {
        return p;
    }

    public void cancel() {
        if (!canCancel)
            return;

        runningPrimary = false;
        runningSecondary = false;
        onCancel();
    }

    @Override
    public final void usePrimary(float targetX, float targetY) {
        if (runningPrimary || !canUsePrimary)
            return;

        runningPrimary = true;
        onUsePrimary(targetX, targetY);
    }

    protected abstract void onUsePrimary(float targetX, float targetY);

    @Override
    public final void useSecondary(float targetX, float targetY) {
        if (runningSecondary || !canUseSecondary)
            return;

        runningSecondary = true;
        onUseSecondary(targetX, targetY);
    }

    protected abstract void onUseSecondary(float targetX, float targetY);

    protected final void end() {
        end(baseCooldown);
    }

    protected final void end(long basecooldown) {
        if (!runningSecondary && !runningPrimary) {
            return;
        }

        PlayableEntity p = owner();

        runningPrimary = false;
        runningSecondary = false;

        long wait = p.calculateFireRate(basecooldown);
        TimeUtils.executeInSync(wait, () -> {
            canUsePrimary = true;
            canUseSecondary = true;
        }, p.getWorld());
    }

    protected final void endSecondary() {
        endSecondary(baseCooldown);
    }

    protected final void endSecondary(long basecooldown) {
        if (!runningSecondary) {
            return;
        }

        PlayableEntity p = owner();

        runningSecondary = false;

        long wait = p.calculateFireRate(basecooldown);
        TimeUtils.executeInSync(wait, () -> canUseSecondary = true, p.getWorld());
    }

    protected final void endPrimary() {
        endPrimary(baseCooldown);
    }

    protected final void endPrimary(long basecooldown) {
        if (!runningPrimary) {
            return;
        }

        PlayableEntity p = owner();

        runningPrimary = false;

        long wait = p.calculateFireRate(basecooldown);
        TimeUtils.executeInSync(wait, () -> canUsePrimary = true, p.getWorld());
    }

    /**
     * Ensure when the later task is executed, it's executed during a tick where
     * the ability is still running.
     * @param duration
     * @param runnable
     */
    protected void executeInSync(long duration, Runnable runnable) {
        TimeUtils.executeInSync(duration, () -> {
            if (runningPrimary || runningSecondary) {
                runnable.run();
            }
        }, owner().getWorld());
    }

    @Override
    public boolean canFirePrimary() {
        return !runningPrimary && canUsePrimary;
    }

    @Override
    public boolean canFireSecondary() {
        return !runningSecondary && canUseSecondary;
    }

    protected void onCancel() {
        end(baseCooldown);
    }
}
