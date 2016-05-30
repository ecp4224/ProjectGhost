package com.boxtrotstudio.ghost.game.match.entities.items;


import com.boxtrotstudio.ghost.game.match.LiveMatch;

public class SpeedItemEntity extends ItemEntity {

    public SpeedItemEntity(LiveMatch match) {
        super(match);
    }

    @Override
    public short getType() {
        return 10;
    }
}
