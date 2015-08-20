package me.eddiep.ghost.game.match.entities.items;


import me.eddiep.ghost.game.match.LiveMatch;

public class InvisibleItemEntity extends ItemEntity {

    public InvisibleItemEntity(LiveMatch match) {
        super(match);
    }

    @Override
    public byte getType() {
        return 13;
    }
}