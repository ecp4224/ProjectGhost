package com.boxtrotstudio.ghost.game.match.abilities;

import com.boxtrotstudio.ghost.game.match.Event;
import com.boxtrotstudio.ghost.game.match.entities.PlayableEntity;
import com.boxtrotstudio.ghost.game.match.stats.BuffType;
import com.boxtrotstudio.ghost.utils.*;

import java.util.ArrayList;

public class Circle extends PlayerAbility {
    private PlayableEntity p;
    private static final long STAGE1_DURATION = 700;
    private static final long STAGE2_DURATION = 600;
    private static final long BASE_COOLDOWN = 1000;

    public Circle(PlayableEntity owner) {
        super(owner);
        baseCooldown = BASE_COOLDOWN;
    }

    @Override
    public String name() {
        return "Circle";
    }

    @Override
    public PlayableEntity owner() {
        return p;
    }

    @Override
    public void onUsePrimary(float targetX, float targetY) {
        wasInside.clear();
        p.setVisible(true);

        double temp = NetworkUtils.storeFloats(targetX, targetY);
        p.triggerEvent(Event.FireCircle, temp); //send double

        Vector2f[] hitbox = VectorUtils.createCircle(128f / 2f, 5, targetX, targetY);
        final HitboxHelper.HitboxToken token = HitboxHelper.checkHitboxEveryTick(hitbox, p, STAGE1);

        //p.getWorld().spawnParticle(ParticleEffect.CIRCLE, (int) (STAGE1_DURATION + STAGE2_DURATION), 64, targetX, targetY, STAGE1_DURATION);

<<<<<<< HEAD
        executeInSync(STAGE1_DURATION, () -> {
            token.useDefaultBehavior();
            for (PlayableEntity p1 : wasInside) {
                p1.setVisible(false);
                p1.getSpeedStat().removeBuff("circle_debuff");
            }
            wasInside.clear();

            executeInSync(STAGE2_DURATION, () -> {
                token.stopChecking();
                p.onFire();

                endPrimary();
            });
        });
    }

    @Override
    protected void onUseSecondary(float targetX, float targetY) {
        endSecondary();
=======
        TimeUtils.executeInSync(STAGE1_DURATION, () -> {
            token.useDefaultBehavior();
            for (PlayableEntity p : wasInside) {
                p.setVisible(false);
                p.getSpeedStat().removeBuff("circle_debuff");
            }
            wasInside.clear();

            TimeUtils.executeInSync(STAGE2_DURATION, () -> {
                token.stopChecking();
                p.onFire();

                long wait = p.calculateFireRate(BASE_COOLDOWN); //Base value is 315ms
                TimeUtils.executeInSync(wait, () -> p.setCanFire(true), p.getWorld());
            }, p.getWorld());
        }, p.getWorld());
        //TimeUtils.executeInSync()
>>>>>>> master
    }

    @Override
    public byte id() {
        return 2;
    }

    private ArrayList<PlayableEntity> wasInside = new ArrayList<>();
<<<<<<< HEAD
    private final P2Runnable<PlayableEntity, Boolean> STAGE1 = (p1, didHit) -> {
        if (didHit) {
            p1.setVisible(true);
            if (!p1.getSpeedStat().hasBuff("circle_debuff")) {
                p1.getSpeedStat().addBuff("circle_debuff", BuffType.PercentSubtraction, 30.0, false);
            }
            wasInside.add(p1);
        } else if (wasInside.contains(p1)) {
            p1.setVisible(false);
            p1.getSpeedStat().removeBuff("circle_debuff");
            wasInside.remove(p1);
=======
    private final P2Runnable<PlayableEntity, Boolean> STAGE1 = (p, didHit) -> {
        if (didHit) {
            p.setVisible(true);
            if (!p.getSpeedStat().hasBuff("circle_debuff")) {
                p.getSpeedStat().addBuff("circle_debuff", BuffType.PercentSubtraction, 30.0, false);
            }
            wasInside.add(p);
        } else if (wasInside.contains(p)) {
            p.setVisible(false);
            p.getSpeedStat().removeBuff("circle_debuff");
            wasInside.remove(p);
>>>>>>> master
        }
    };
}
