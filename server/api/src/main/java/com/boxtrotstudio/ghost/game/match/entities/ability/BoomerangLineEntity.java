package com.boxtrotstudio.ghost.game.match.entities.ability;

import com.boxtrotstudio.ghost.game.match.entities.BaseEntity;
import com.boxtrotstudio.ghost.game.match.entities.TypeableEntity;

public class BoomerangLineEntity extends BaseEntity implements TypeableEntity {
    public BoomerangLineEntity() {
        super();
        setVisible(true);
        setName("LINE");
    }

    @Override
    public short getType() {
        return 6;
    }
}
