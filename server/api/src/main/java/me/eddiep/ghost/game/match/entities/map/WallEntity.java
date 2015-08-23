package me.eddiep.ghost.game.match.entities.map;

import me.eddiep.ghost.game.match.entities.BaseEntity;
import me.eddiep.ghost.game.match.entities.TypeableEntity;

public class WallEntity extends BaseEntity implements TypeableEntity {
    public WallEntity() {
        sendUpdates(false); //Walls don't need updates
        requestTicks(false); //Walls don't need ticks
    }

    @Override
    public void tick() { }

    @Override
    public byte getType() {
        return -127; //Items should start at -127
    }
}
