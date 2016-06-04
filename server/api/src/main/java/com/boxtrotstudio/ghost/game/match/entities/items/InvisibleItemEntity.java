package com.boxtrotstudio.ghost.game.match.entities.items;


import com.boxtrotstudio.ghost.game.match.LiveMatch;

public class InvisibleItemEntity extends ItemEntity {

    public InvisibleItemEntity(LiveMatch match) {
        super(match);
    }

    @Override
    public short getType() {
        return 13;
    }
}