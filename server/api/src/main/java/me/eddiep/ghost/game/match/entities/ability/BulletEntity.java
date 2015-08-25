package me.eddiep.ghost.game.match.entities.ability;

import me.eddiep.ghost.game.match.entities.BaseEntity;
import me.eddiep.ghost.game.match.entities.PlayableEntity;
import me.eddiep.ghost.game.match.entities.TypeableEntity;
import me.eddiep.ghost.game.match.entities.playable.impl.BaseNetworkPlayer;
import me.eddiep.ghost.utils.Vector2f;

public class BulletEntity extends BaseEntity implements TypeableEntity {
    private PlayableEntity parent;
    public BulletEntity(PlayableEntity parent) {
        super();
        setParent(parent);
        setMatch(parent.getMatch());
        setVisible(true);
        setName("BULLET");
        this.parent = parent;
    }

    @Override
    public void tick() {
        position.x += velocity.x;
        position.y += velocity.y;

        PlayableEntity[] opponents = parent.getOpponents();
        for (PlayableEntity toHit : opponents) {
            if (toHit.isDead())
                continue;

            if (isInside(toHit.getX() - (BaseNetworkPlayer.WIDTH / 2f),
                    toHit.getY() - (BaseNetworkPlayer.HEIGHT / 2f),
                    toHit.getX() + (BaseNetworkPlayer.WIDTH / 2f),
                    toHit.getY() + (BaseNetworkPlayer.HEIGHT / 2f))) {

                toHit.subtractLife();
                if (!toHit.isVisible()) {
                    toHit.setVisible(true);
                }

                toHit.onDamage(parent); //p was damaged by the parent

                world.despawnEntity(this);
                parent.onDamagePlayable(toHit); //the parent damaged p
                if (toHit.isDead()) {
                    parent.onKilledPlayable(toHit);
                }
            }
        }

        Vector2f lower = containingMatch.getLowerBounds();
        Vector2f upper = containingMatch.getUpperBounds();

        if (position.x < lower.x ||
            position.x > upper.x ||
            position.y < lower.y ||
            position.y > upper.y) {
            world.despawnEntity(this);
            parent.onShotMissed();
        }
    }

    @Override
    public short getType() {
        return 2;
    }
}
