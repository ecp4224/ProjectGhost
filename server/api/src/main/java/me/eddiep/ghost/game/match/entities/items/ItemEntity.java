package me.eddiep.ghost.game.match.entities.items;


import me.eddiep.ghost.game.match.LiveMatch;
import me.eddiep.ghost.game.match.entities.BaseEntity;
import me.eddiep.ghost.game.match.entities.PlayableEntity;
import me.eddiep.ghost.game.match.entities.TypeableEntity;
import me.eddiep.ghost.game.match.entities.playable.impl.BaseNetworkPlayer;

public abstract class ItemEntity extends BaseEntity implements TypeableEntity {

    public ItemEntity(LiveMatch match) {
        setName("ITEM");
        setMatch(match);
        setVelocity(0.0f, 0.0f);
        setVisible(true);
        requestTicks(false); //Items don't need ticks
        sendUpdates(false); //Items don't need to send updates
    }

    public boolean intersects(PlayableEntity player) {
        return isInside(player.getX() - (BaseNetworkPlayer.WIDTH / 2f),
                player.getY() - (BaseNetworkPlayer.HEIGHT / 2f),
                player.getX() + (BaseNetworkPlayer.WIDTH / 2f),
                player.getY() + (BaseNetworkPlayer.HEIGHT / 2f));
    }
}
