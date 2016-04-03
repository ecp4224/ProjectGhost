package com.boxtrotstudio.ghost.game.match.item;

import com.boxtrotstudio.ghost.game.match.entities.items.InvisibleItemEntity;
import com.boxtrotstudio.ghost.game.match.entities.items.ItemEntity;
import com.boxtrotstudio.ghost.game.match.LiveMatch;

/**
 * An item that makes the player invisible, regardless of firing, for 5 seconds.
 */
public class InvisibleItem extends Item {
    byte currentLives;
    public InvisibleItem(LiveMatch match) {
        super(match);
    }

    @Override
    public long getDuration() {
        return 10_000;
    }

    @Override
    protected Class<? extends ItemEntity> getEntityClass() {
        return InvisibleItemEntity.class;
    }

    @Override
    protected void onActivated() {
        currentLives = activator.getLives();
    }

    @Override
    protected void handleLogic() {
        if(activator.getAlpha() > 0){
            activator.setAlpha(0);
        }
        if (System.currentTimeMillis() - activationTime >= 5_000) {
            deactivate();
        }
    }
}