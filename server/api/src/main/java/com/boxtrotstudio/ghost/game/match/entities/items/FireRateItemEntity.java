package com.boxtrotstudio.ghost.game.match.entities.items;

import com.boxtrotstudio.ghost.game.match.LiveMatch;

public class FireRateItemEntity extends ItemEntity {

    public FireRateItemEntity(LiveMatch match) {
        super(match);
    }

    @Override
    public short getType() {
        return 14;
    }
}
