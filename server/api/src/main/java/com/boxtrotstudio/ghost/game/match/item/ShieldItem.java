package com.boxtrotstudio.ghost.game.match.item;

import com.boxtrotstudio.ghost.game.match.LiveMatch;
import com.boxtrotstudio.ghost.game.match.entities.items.ItemEntity;
import com.boxtrotstudio.ghost.game.match.entities.items.ShieldItemEntity;

/**
 * An item that makes the player invulnerable to attacks for 5 seconds.
 */
public class ShieldItem extends Item {
    public ShieldItem(LiveMatch match) {
        super(match);
    }

    @Override
    public long getDuration() {
        return 10_000;
    }

    @Override
    protected Class<? extends ItemEntity> getEntityClass() {
        return ShieldItemEntity.class;
    }

    @Override
    protected void onActivated() {
        activator.addInvincibilityStack();
    }

    @Override
    protected void handleLogic() {
        if (System.currentTimeMillis() - activationTime >= 5_000) {
            activator.removeInvincibilityStack();
            deactivate();
        }
    }
}