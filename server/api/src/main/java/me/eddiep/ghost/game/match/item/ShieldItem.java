package me.eddiep.ghost.game.match.item;

import me.eddiep.ghost.game.match.entities.items.ItemEntity;
import me.eddiep.ghost.game.match.entities.items.ShieldItemEntity;
import me.eddiep.ghost.game.match.LiveMatch;

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
        activator.isInvincible(true);
    }

    @Override
    protected void handleLogic() {
        if (System.currentTimeMillis() - activationTime >= 5_000) {
            activator.isInvincible(false);
            deactivate();
        }
    }
}