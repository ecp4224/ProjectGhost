package com.boxtrotstudio.ghost.game.match.entities.items;


import com.boxtrotstudio.ghost.game.match.LiveMatch;
import com.boxtrotstudio.ghost.game.match.entities.BaseEntity;
import com.boxtrotstudio.ghost.game.match.entities.TypeableEntity;

public abstract class ItemEntity extends BaseEntity implements TypeableEntity {

    public ItemEntity(LiveMatch match) {
        setName("ITEM");
        setMatch(match);
        setVelocity(0.0f, 0.0f);
        setVisible(true);
        requestTicks(false); //Items don't need ticks
        sendUpdates(false); //Items don't need to send updates
    }


}
