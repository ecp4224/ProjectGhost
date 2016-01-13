package me.eddiep.ghost.game.match.abilities;

import me.eddiep.ghost.game.match.Event;
import me.eddiep.ghost.game.match.entities.PlayableEntity;
import me.eddiep.ghost.game.match.stats.BuffType;
import me.eddiep.ghost.utils.*;

import java.util.ArrayList;

public class Circle implements Ability<PlayableEntity> {
    private PlayableEntity p;
    private static final long STAGE1_DURATION = 700;
    private static final long STAGE2_DURATION = 600;
    private static final long BASE_COOLDOWN = 1000;

    public Circle(PlayableEntity owner) {
        this.p = owner;
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
    public void use(float targetX, float targetY) {
        wasInside.clear();
        p.setCanFire(false);
        p.setVisible(true);

        double temp = NetworkUtils.storeFloats(targetX, targetY);
        p.triggerEvent(Event.FireCircle, temp); //send double

        Vector2f[] hitbox = VectorUtils.createCircle(128f / 2f, 5, targetX, targetY);
        final HitboxHelper.HitboxToken token = HitboxHelper.checkHitboxEveryTick(hitbox, p, STAGE1);

        //p.getWorld().spawnParticle(ParticleEffect.CIRCLE, (int) (STAGE1_DURATION + STAGE2_DURATION), 64, targetX, targetY, STAGE1_DURATION);

        TimeUtils.executeInSync(STAGE1_DURATION, new Runnable() {
            @Override
            public void run() {
                token.useDefaultBehavior();
                for (PlayableEntity p : wasInside) {
                    p.setVisible(false);
                    p.getSpeedStat().removeBuff("circle_debuff");
                }
                wasInside.clear();

                TimeUtils.executeInSync(STAGE2_DURATION, new Runnable() {
                    @Override
                    public void run() {
                        token.stopChecking();
                        p.onFire();

                        long wait = p.calculateFireRate(BASE_COOLDOWN); //Base value is 315ms
                        TimeUtils.executeInSync(wait, new Runnable() {
                            @Override
                            public void run() {
                                p.setCanFire(true);
                            }
                        }, p.getWorld());
                    }
                }, p.getWorld());
            }
        }, p.getWorld());
        //TimeUtils.executeInSync()
    }

    @Override
    public byte id() {
        return 2;
    }

    private ArrayList<PlayableEntity> wasInside = new ArrayList<>();
    private final P2Runnable<PlayableEntity, Boolean> STAGE1 = new P2Runnable<PlayableEntity, Boolean>() {
        @Override
        public void run(PlayableEntity p, Boolean didHit) {
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
            }
        }
    };
}
