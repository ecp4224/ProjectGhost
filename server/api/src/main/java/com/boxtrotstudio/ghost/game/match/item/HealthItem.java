package com.boxtrotstudio.ghost.game.match.item;

import com.boxtrotstudio.ghost.game.match.LiveMatch;
import com.boxtrotstudio.ghost.game.match.entities.items.HealthItemEntity;
import com.boxtrotstudio.ghost.game.match.entities.items.ItemEntity;

/**
 * An item that adds 1 to the player's current lives.
 */
public class HealthItem extends Item {

    public HealthItem(LiveMatch match) {
        super(match);
    }

    @Override
    public long getDuration() {
        return 10_000;
    }

    @Override
    protected Class<? extends ItemEntity> getEntityClass() {
        return HealthItemEntity.class;
    }

    @Override
    protected void onActivated() {
        activator.setLives((byte) (activator.getLives() + 1));
    }

    @Override
    protected void handleLogic() {
        deactivate(); //Deactivate after first tick
    }
}
