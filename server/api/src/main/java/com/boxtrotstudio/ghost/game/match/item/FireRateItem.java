package com.boxtrotstudio.ghost.game.match.item;

import com.boxtrotstudio.ghost.game.match.entities.items.ItemEntity;
import com.boxtrotstudio.ghost.game.match.stats.BuffType;
import com.boxtrotstudio.ghost.game.match.LiveMatch;
import com.boxtrotstudio.ghost.game.match.entities.items.FireRateItemEntity;

public class FireRateItem extends Item {

    public FireRateItem(LiveMatch match) {
        super(match);
    }

    @Override
    public long getDuration() {
        return 10_000;
    }

    @Override
    protected Class<? extends ItemEntity> getEntityClass() {
        return FireRateItemEntity.class;
    }

    @Override
    protected void onActivated() {
        activator.getFireRateStat().addBuff("fire_rate_buff", BuffType.Addition, 80); //Add 20% to firerate
        activator.onStatUpdate(activator.getFireRateStat());
    }

    @Override
    protected void handleLogic() {
        if (System.currentTimeMillis() - activationTime >= 10_000) {
            activator.getFireRateStat().removeBuff("fire_rate_buff");
            activator.onStatUpdate(activator.getFireRateStat());
            deactivate();
        }
    }
}