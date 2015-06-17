package me.eddiep.ghost.server.game.entities;

import me.eddiep.ghost.server.game.ActiveMatch;
import me.eddiep.ghost.server.game.BaseEntity;
import me.eddiep.ghost.server.game.entities.playable.impl.Player;

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
            if (isInside(toHit.getX() - (Player.WIDTH / 2f),
                    toHit.getY() - (Player.HEIGHT / 2f),
                    toHit.getX() + (Player.WIDTH / 2f),
                    toHit.getY() + (Player.HEIGHT / 2f))) {

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

        if (position.x < ActiveMatch.MAP_XMIN ||
            position.x > ActiveMatch.MAP_XMAX ||
            position.y < ActiveMatch.MAP_YMIN ||
            position.y > ActiveMatch.MAP_YMAX) {
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
