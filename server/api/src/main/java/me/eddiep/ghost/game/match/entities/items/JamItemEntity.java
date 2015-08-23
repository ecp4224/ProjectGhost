package me.eddiep.ghost.game.match.entities.items;


import me.eddiep.ghost.game.match.LiveMatch;

public class JamItemEntity extends ItemEntity {

    public JamItemEntity(LiveMatch match) {
        super(match);
    }

    @Override
    public byte getType() {
        return 15;
    }
}