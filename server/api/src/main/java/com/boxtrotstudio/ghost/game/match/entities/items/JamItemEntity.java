package com.boxtrotstudio.ghost.game.match.entities.items;


import com.boxtrotstudio.ghost.game.match.LiveMatch;

public class JamItemEntity extends ItemEntity {

    public JamItemEntity(LiveMatch match) {
        super(match);
    }

    @Override
    public short getType() {
        return 15;
    }
}