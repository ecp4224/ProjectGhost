package me.eddiep.ghost.game.entities.items;

import me.eddiep.ghost.game.BaseEntity;
import me.eddiep.ghost.game.entities.TypeableEntity;
import me.eddiep.ghost.game.entities.playable.impl.BaseNetworkPlayer;

public abstract class ItemEntity extends BaseEntity implements TypeableEntity {

    @Override
    public void tick() {

    }

    public boolean intersects(BaseNetworkPlayer player) {
        return isInside(player.getX() - (BaseNetworkPlayer.WIDTH / 2f),
                player.getY() - (BaseNetworkPlayer.HEIGHT / 2f),
                player.getX() + (BaseNetworkPlayer.WIDTH / 2f),
                player.getY() + (BaseNetworkPlayer.HEIGHT / 2f));
    }
}
