package me.eddiep.ghost.game.match.item;

import me.eddiep.ghost.game.match.LiveMatch;
import me.eddiep.ghost.game.match.entities.items.FireRateItemEntity;
import me.eddiep.ghost.game.match.entities.items.ItemEntity;
import me.eddiep.ghost.game.match.stats.BuffType;

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
        activator.getFireRateStat().addBuff("fire_rate_buff", BuffType.PercentSubtraction, 50);
        activator.onStatUpdate(activator.getFireRateStat());
    }

    @Override
    protected void handleLogic() {
        if (System.currentTimeMillis() - activationTime >= 10_000) {
            activator.getFireRateStat().removeBuff("fire_rate_buff");
            activator.onStatUpdate(activator.getFireRateStat());
            match.despawnItem(this);
        }
    }
}