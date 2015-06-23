package me.eddiep.ghost.game.entities;

import me.eddiep.ghost.game.BaseEntity;
import me.eddiep.ghost.game.entities.playable.impl.BaseNetworkPlayer;
import me.eddiep.ghost.utils.Vector2f;

import java.io.IOException;

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
            if (isInside(toHit.getX() - (BaseNetworkPlayer.WIDTH / 2f),
                    toHit.getY() - (BaseNetworkPlayer.HEIGHT / 2f),
                    toHit.getX() + (BaseNetworkPlayer.WIDTH / 2f),
                    toHit.getY() + (BaseNetworkPlayer.HEIGHT / 2f))) {

                toHit.subtractLife();
                if (!toHit.isVisible()) {
                    toHit.setVisible(true);
                }

                toHit.onDamage(parent); //p was damaged by the parent

                try {
                    getMatch().despawnEntity(this);
                    parent.onDamagePlayable(toHit); //the parent damaged p
                    if (toHit.isDead()) {
                        parent.onKilledPlayable(toHit);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        Vector2f lower = containingMatch.getLowerBounds();
        Vector2f upper = containingMatch.getUpperBounds();

        if (position.x < lower.x ||
            position.x > upper.x ||
            position.y < lower.y ||
            position.y > upper.y) {
            try {
                getMatch().despawnEntity(this);
                parent.onShotMissed();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public byte getType() {
        return 2;
    }
}
