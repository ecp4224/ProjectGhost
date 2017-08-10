package com.boxtrotstudio.ghost.game.match.entities.items;


import com.boxtrotstudio.ghost.game.match.LiveMatch;

public class EmpItemEntity extends ItemEntity {

    public EmpItemEntity(LiveMatch match) {
        super(match);
    }

    @Override
    public short getType() {
        return 16;
    }
}