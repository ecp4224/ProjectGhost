package me.eddiep.ghost.game.match.item;

import me.eddiep.ghost.game.match.entities.items.ItemEntity;
import me.eddiep.ghost.game.match.entities.items.ShieldItemEntity;
import me.eddiep.ghost.game.match.LiveMatch;

/**
 * An item that makes the player invulnerable to attacks for 5 seconds.
 */
public class ShieldItem extends Item {
    byte currentLives;
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
        currentLives = activator.getLives();
    }

    @Override
    protected void handleLogic() {
        if(activator.getLives() < currentLives){
            activator.setLives(currentLives);
        }
        if (System.currentTimeMillis() - activationTime >= 5_000) {
            match.despawnItem(this);
        }
    }
}