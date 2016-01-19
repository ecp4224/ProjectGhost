package me.eddiep.ghost.client.core.game;

import me.eddiep.ghost.client.core.Entity;
import me.eddiep.ghost.client.core.EntityFactory;

public enum Item {
    SPEED(10),
    HEALTH(11),
    SHIELD(12),
    INVISIBLE(13),
    EMP(14),
    JAM(15),
    FIRERATE(16),
    UNKNOWN(-1);

    private short entityID;
    Item(int id) {
        this((short)id);
    }

    Item(short id) {
        this.entityID = id;
    }

    public Entity createItem(short id) {
        return EntityFactory.createEntity(entityID, id);
    }

    public short getID() {
        return entityID;
    }

    public static Item getItem(short id) {
        for (Item item : values()) {
            if (item.getID() == id)
                return item;
        }

        return UNKNOWN;
    }
}
