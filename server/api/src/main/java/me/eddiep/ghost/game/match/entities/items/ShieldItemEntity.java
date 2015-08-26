package me.eddiep.ghost.game.match.entities.items;


import me.eddiep.ghost.game.match.LiveMatch;

public class ShieldItemEntity extends ItemEntity {

    public ShieldItemEntity(LiveMatch match) {
        super(match);
    }

    @Override
    public short getType() {
        return 12;
    }
}
