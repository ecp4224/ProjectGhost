package me.eddiep.ghost.game.match.entities.items;


import me.eddiep.ghost.game.match.LiveMatch;

public class EmpItemEntity extends ItemEntity {

    public EmpItemEntity(LiveMatch match) {
        super(match);
    }

    @Override
    public byte getType() {
        return 14;
    }
}