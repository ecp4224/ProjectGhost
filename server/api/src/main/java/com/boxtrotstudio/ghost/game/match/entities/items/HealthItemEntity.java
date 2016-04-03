package com.boxtrotstudio.ghost.game.match.entities.items;


import com.boxtrotstudio.ghost.game.match.LiveMatch;

public class HealthItemEntity extends ItemEntity {

    public HealthItemEntity(LiveMatch match) {
        super(match);
    }

    @Override
    public short getType() {
        return 11;
    }
}
