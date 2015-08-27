package me.eddiep.ghost.utils;

import me.eddiep.ghost.game.match.entities.PlayableEntity;
import me.eddiep.ghost.game.match.entities.ability.BulletEntity;
import me.eddiep.ghost.network.Server;

import java.util.ArrayList;

public class HitboxHelper implements Tickable {
    private Server server;
    private PlayableEntity entity;
    private Vector2f[] hitbox;
    private HitboxToken token;
    private ArrayList<PlayableEntity> alreadyHit = new ArrayList<>();

    /**
     * Check the provided hitbox every server tick for the {@link me.eddiep.ghost.game.match.entities.PlayableEntity} <b>playableEntity</b>
     * This function will return a {@link me.eddiep.ghost.utils.HitboxHelper.HitboxToken} which can be used to stop the checking of this hitbox.
     * @param hitbox The hitbox to check
     * @param playableEntity The damager for this check
     * @param server The server to execute the ticks on
     * @return A {@link me.eddiep.ghost.utils.HitboxHelper.HitboxToken} that can be used to stop checking the hitbox in a future
     */
    public static HitboxToken checkHitboxEveryTick(Vector2f[] hitbox, PlayableEntity playableEntity, Server server) {
        HitboxHelper helper = new HitboxHelper();
        helper.server = server;
        helper.entity = playableEntity;
        helper.hitbox = hitbox;

        HitboxToken token = new HitboxToken();
        helper.token = token;
        token.helper = helper;

        helper.startChecking();

        return token;
    }

    private void startChecking() {
        PlayableEntity[] opponents = entity.getOpponents();

        for (PlayableEntity p : opponents) {
            if (alreadyHit.contains(p))
                continue;
            if (p.isDead())
                continue;

            if (VectorUtils.isPointInside(p.getPosition(), hitbox)) {
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
            }
        }

        if (token.isChecking()) {
            Global.DEFAULT_SERVER.executeNextTick(this);
        } else {
            alreadyHit.clear();
            entity = null;
            hitbox = null;
            server = null;
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
            for (Vector2f points : helper.hitbox) {
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
    }
}
