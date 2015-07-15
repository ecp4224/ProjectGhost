package me.eddiep.ghost.game.match.item;

import me.eddiep.ghost.game.match.entities.items.ItemEntity;
import me.eddiep.ghost.game.match.entities.items.SpeedItemEntity;
import me.eddiep.ghost.game.match.LiveMatch;

/**
 * An item that speeds the player up for 10 seconds.
 */
public class SpeedItem extends Item {

    public SpeedItem(LiveMatch match) {
        super(match);
    }

    @Override
    public long getDuration() {
        return 10_000;
    }

    @Override
    protected Class<? extends ItemEntity> getEntityClass() {
        return SpeedItemEntity.class;
    }

    @Override
    protected void onActivated() {
        activator.setSpeed(18.0f);
    }

    @Override
    protected void handleLogic() {
        if (System.currentTimeMillis() - activationTime >= 10_000) {
            activator.setSpeed(6.0f);
            match.despawnItem(this);
        }
    }
}
