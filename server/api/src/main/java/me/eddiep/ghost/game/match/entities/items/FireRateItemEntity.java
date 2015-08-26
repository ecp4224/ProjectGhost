package me.eddiep.ghost.game.match.entities.items;

import me.eddiep.ghost.game.match.LiveMatch;

public class FireRateItemEntity extends ItemEntity {

    public FireRateItemEntity(LiveMatch match) {
        super(match);
    }

    @Override
    public short getType() {
        return 16;
    }
}
