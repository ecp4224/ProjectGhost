package com.boxtrotstudio.ghost.utils;

import com.boxtrotstudio.ghost.game.match.entities.PlayableEntity;
import com.boxtrotstudio.ghost.game.match.entities.ability.BulletEntity;
import com.boxtrotstudio.ghost.game.match.world.World;
import com.boxtrotstudio.ghost.game.match.world.physics.CollisionResult;
import com.boxtrotstudio.ghost.game.match.world.physics.PolygonHitbox;
import com.boxtrotstudio.ghost.utils.tick.Tickable;

import java.security.InvalidParameterException;
import java.util.ArrayList;

public class HitboxHelper implements Tickable {
    private World world;
    private PlayableEntity entity;
    private PolygonHitbox hitbox;

    private boolean doesGrow;
    private Vector2f[] originalHitbox;
    private float growSpeed;
    private float growPos;

    private HitboxToken token;
    private ArrayList<PlayableEntity> alreadyHit = new ArrayList<>();
    private P2Runnable<PlayableEntity, Boolean> customAction;

    /**
     * Check the provided hitbox every server tick for the {@link PlayableEntity} <b>playableEntity</b>
     * This function will return a {@link HitboxHelper.HitboxToken} which can be used to stop the checking of this hitbox.
     * @param hitbox The hitbox to check
     * @param playableEntity The damager for this check
     * @return A {@link HitboxHelper.HitboxToken} that can be used to stop checking the hitbox in a future
     */
    public static HitboxToken checkHitboxEveryTick(Vector2f[] hitbox, PlayableEntity playableEntity) {
        return checkHitboxEveryTick(hitbox, playableEntity, null);
    }

    /**
     * Check the provided hitbox every server tick for the {@link PlayableEntity} <b>playableEntity</b>
     * This function will return a {@link HitboxHelper.HitboxToken} which can be used to stop the checking of this hitbox.
     * @param hitbox The hitbox to check
     * @param playableEntity The damager for this check
     * @param behavior The custom function to run when a collision is detected
     * @return A {@link HitboxHelper.HitboxToken} that can be used to stop checking the hitbox in a future
     */
    public static HitboxToken checkHitboxEveryTick(Vector2f[] hitbox, PlayableEntity playableEntity, P2Runnable<PlayableEntity, Boolean> behavior) {
        return checkHitboxEveryTick(hitbox, playableEntity, behavior, false, 0L);
    }

    /**
     * Check the provided hitbox every server tick for the {@link PlayableEntity} <b>playableEntity</b>
     * This function will return a {@link HitboxHelper.HitboxToken} which can be used to stop the checking of this hitbox.
     * @param hitbox The hitbox to check
     * @param playableEntity The damager for this check
     * @param behavior The custom function to run when a collision is detected
     * @param doesGrow Whether this hitbox grows (ex; a laser)
     * @param growSpeed How fast the hitbox grows in pixels, if this hitbox grows
     * @return A {@link HitboxHelper.HitboxToken} that can be used to stop checking the hitbox in a future
     */
    public static HitboxToken checkHitboxEveryTick(Vector2f[] hitbox, PlayableEntity playableEntity, P2Runnable<PlayableEntity, Boolean> behavior, boolean doesGrow, float growSpeed) {
        return checkHitboxEveryTick(hitbox, playableEntity, behavior, doesGrow, growSpeed, 0);
    }

    /**
     * Check the provided hitbox every server tick for the {@link PlayableEntity} <b>playableEntity</b>
     * This function will return a {@link HitboxHelper.HitboxToken} which can be used to stop the checking of this hitbox.
     * @param hitbox The hitbox to check
     * @param playableEntity The damager for this check
     * @param behavior The custom function to run when a collision is detected
     * @param doesGrow Whether this hitbox grows (ex; a laser)
     * @param growSpeed How fast the hitbox grows in pixels, if this hitbox grows
     * @return A {@link HitboxHelper.HitboxToken} that can be used to stop checking the hitbox in a future
     */
    public static HitboxToken checkHitboxEveryTick(Vector2f[] hitbox, PlayableEntity playableEntity, P2Runnable<PlayableEntity, Boolean> behavior, boolean doesGrow, float growSpeed, float growPos) {
        if (playableEntity.getWorld() == null) {
            throw new InvalidParameterException("This playableEntity is not in a world!");
        }

        HitboxHelper helper = new HitboxHelper();
        helper.world = playableEntity.getWorld();
        helper.entity = playableEntity;
        helper.hitbox = new PolygonHitbox(playableEntity.getName() + "_HITBOX", hitbox);
        helper.customAction = behavior;
        helper.doesGrow = doesGrow;

        if (doesGrow) {
            helper.originalHitbox = hitbox;
            helper.growSpeed = growSpeed;
            helper.growPos = growPos;
        }

        HitboxToken token = new HitboxToken();
        helper.token = token;
        token.helper = helper;

        helper.startChecking();

        return token;
    }

    private void startChecking() {
        PlayableEntity[] opponents = entity.getOpponents();

        for (PlayableEntity p : opponents) {
            CollisionResult result = hitbox.isHitboxInside(p.getHitbox());

            boolean didHit = hitbox.isHitboxInside(p.getHitbox()) != CollisionResult.NO_HIT;
            if (didHit) {
                if (alreadyHit.contains(p))
                    continue;
                if (p.isDead())
                    continue;

                if (doesGrow) {
                    growPos += growSpeed;

                    Vector2f midpoint = VectorUtils.midpoint(hitbox.getPolygon().getPoints()[0], hitbox.getPolygon().getPoints()[1]);
                    Vector2f pointOfContact = result.getPointOfContact();

                    double distance = Vector2f.distance(midpoint, pointOfContact);

                    if (distance > growPos)
                        continue;
                }

                if (customAction == null) {
                    p.subtractLife();
                    if (!p.isVisible()) {
                        p.setVisible(true);
                    }

                    p.onDamage(entity); //p was damaged by the parent

                    entity.onDamagePlayable(p); //the parent damaged p
                    if (p.isDead()) {
                        entity.onKilledPlayable(p);
                    }

                    alreadyHit.add(p);
                } else {
                    customAction.run(p, true);
                }
            } else if (customAction != null) {
                customAction.run(p, false);
            }
        }

        if (token.isChecking()) {
            world.executeNextTick(this);
        } else {
            alreadyHit.clear();
            entity = null;
            hitbox = null;
            world = null;
            token = null;
        }
    }

    private HitboxHelper() { }

    @Override
    public void tick() {
        startChecking();
    }

    public static class HitboxToken {
        private boolean stopChecking;
        public HitboxHelper helper;

        private HitboxToken() { }

        public void displayHitbox() {
            for (Vector2f points : helper.hitbox.getPolygon().getPoints()) {
                BulletEntity point = new BulletEntity(helper.entity);
                point.setPosition(points);
                point.setVelocity(new Vector2f(0f, 0f));
                point.requestTicks(false);
                helper.entity.getWorld().spawnEntity(point);
            }
        }

        public boolean isChecking() {
            return !stopChecking;
        }

        public void stopChecking() {
            stopChecking = true;
        }

        public void useDefaultBehavior() {
            helper.customAction = null;
        }

        public void useBehavior(P2Runnable<PlayableEntity, Boolean> func) {
            helper.customAction = func;
        }
    }
}
