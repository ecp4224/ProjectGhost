package me.eddiep.ghost.utils;

import me.eddiep.ghost.game.match.entities.PlayableEntity;
import me.eddiep.ghost.game.match.entities.ability.BulletEntity;
import me.eddiep.ghost.game.match.world.World;
import me.eddiep.ghost.game.match.world.physics.CollisionResult;
import me.eddiep.ghost.game.match.world.physics.PolygonHitbox;
import me.eddiep.ghost.utils.tick.Tickable;

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
     * Check the provided hitbox every server tick for the {@link me.eddiep.ghost.game.match.entities.PlayableEntity} <b>playableEntity</b>
     * This function will return a {@link me.eddiep.ghost.utils.HitboxHelper.HitboxToken} which can be used to stop the checking of this hitbox.
     * @param hitbox The hitbox to check
     * @param playableEntity The damager for this check
     * @return A {@link me.eddiep.ghost.utils.HitboxHelper.HitboxToken} that can be used to stop checking the hitbox in a future
     */
    public static HitboxToken checkHitboxEveryTick(Vector2f[] hitbox, PlayableEntity playableEntity) {
        return checkHitboxEveryTick(hitbox, playableEntity, null);
    }

    /**
     * Check the provided hitbox every server tick for the {@link me.eddiep.ghost.game.match.entities.PlayableEntity} <b>playableEntity</b>
     * This function will return a {@link me.eddiep.ghost.utils.HitboxHelper.HitboxToken} which can be used to stop the checking of this hitbox.
     * @param hitbox The hitbox to check
     * @param playableEntity The damager for this check
     * @param behavor The custom function to run when a collision is detected
     * @return A {@link me.eddiep.ghost.utils.HitboxHelper.HitboxToken} that can be used to stop checking the hitbox in a future
     */
    public static HitboxToken checkHitboxEveryTick(Vector2f[] hitbox, PlayableEntity playableEntity, P2Runnable<PlayableEntity, Boolean> behavor) {
        return checkHitboxEveryTick(hitbox, playableEntity, behavor, false, 0L);
    }

    /**
     * Check the provided hitbox every server tick for the {@link me.eddiep.ghost.game.match.entities.PlayableEntity} <b>playableEntity</b>
     * This function will return a {@link me.eddiep.ghost.utils.HitboxHelper.HitboxToken} which can be used to stop the checking of this hitbox.
     * @param hitbox The hitbox to check
     * @param playableEntity The damager for this check
     * @param behavor The custom function to run when a collision is detected
     * @return A {@link me.eddiep.ghost.utils.HitboxHelper.HitboxToken} that can be used to stop checking the hitbox in a future
     */
    public static HitboxToken checkHitboxEveryTick(Vector2f[] hitbox, PlayableEntity playableEntity, P2Runnable<PlayableEntity, Boolean> behavor, boolean doesGrow, float growSpeed) {
        if (playableEntity.getWorld() == null) {
            throw new InvalidParameterException("This playableEntity is not in a world!");
        }

        HitboxHelper helper = new HitboxHelper();
        helper.world = playableEntity.getWorld();
        helper.entity = playableEntity;
        helper.hitbox = new PolygonHitbox(playableEntity.getName() + "_HITBOX", hitbox);
        helper.customAction = behavor;
        helper.doesGrow = doesGrow;

        if (doesGrow) {
            helper.originalHitbox = hitbox;
            helper.growSpeed = growSpeed;
        }

        HitboxToken token = new HitboxToken();
        helper.token = token;
        token.helper = helper;

        helper.startChecking();

        return token;
    }

    private void startChecking() {
        if (doesGrow) {
            growPos += growSpeed;

            float xDif = originalHitbox[1].x - originalHitbox[0].x;
            float yDif = originalHitbox[2].y - originalHitbox[0].y;

            float bx = originalHitbox[0].x + (xDif * Math.min(1f, (growPos / xDif)));
            float by = originalHitbox[0].y + (yDif * Math.min(1f, (growPos / yDif)));

            System.out.println(originalHitbox[0].x + ", " + originalHitbox[0].y + " : " + bx + ", " + by + " (" + originalHitbox[1].x + ", " + originalHitbox[2].y + ")" + " " + xDif);

            Vector2f[] newHitbox = new Vector2f[] {
                    originalHitbox[0],
                    new Vector2f(bx, originalHitbox[0].y),
                    new Vector2f(bx, by),
                    new Vector2f(originalHitbox[0].x, by)
            };

            this.hitbox = new PolygonHitbox(entity.getName() + "_HITBOX", newHitbox);
            this.token.displayHitbox();
        }

        PlayableEntity[] opponents = entity.getOpponents();

        for (PlayableEntity p : opponents) {
            boolean didHit = hitbox.isHitboxInside(p.getHitbox()) != CollisionResult.NO_HIT;
            if (didHit) {
                if (alreadyHit.contains(p))
                    continue;
                if (p.isDead())
                    continue;

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
                    customAction.run(p, didHit);
                }
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
