package com.boxtrotstudio.ghost.game.match.abilities;

import com.boxtrotstudio.ghost.game.match.entities.PlayableEntity;
import com.boxtrotstudio.ghost.utils.TimeUtils;
import com.boxtrotstudio.ghost.utils.tick.Tickable;

public abstract class CancelableAbility implements Ability<PlayableEntity> {
    private boolean running;
    protected boolean canCancel = true;

    @Override
    public final void use(float targetX, float targetY) {
        running = true;
        onUse(targetX, targetY);
    }

    public boolean isRunning() {
        return running;
    }

    public final void cancel() {
        if (!running || !canCancel)
            return;

        running = false;
        onCancel();
    }

    protected final void end(long basecooldown) {
        if (!running)
            return;

        PlayableEntity p = owner();

        running = false;

        long wait = p.calculateFireRate(basecooldown);
        TimeUtils.executeInSync(wait, () -> p.setCanFire(true), p.getWorld());
    }

    protected void executeInSync(long duration, Runnable runnable) {
        TimeUtils.executeInSync(duration, () -> {
            if (running) {
                runnable.run();
            }
        }, owner().getWorld());
    }

    protected abstract void onUse(float targetX, float targetY);

    protected void onCancel() {

    }
}
