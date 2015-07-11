package me.eddiep.ghost.game.item;

import me.eddiep.ghost.game.LiveMatch;
import me.eddiep.ghost.game.entities.items.ItemEntity;
import me.eddiep.ghost.game.entities.items.SpeedItemEntity;

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
        activator.setSpeed(8.0f);
    }

    @Override
    protected void handleLogic() {
        if (System.currentTimeMillis() - activationTime >= 10_000) {
            activator.setSpeed(6.0f);
            match.despawnItem(this);
        }
    }
}
