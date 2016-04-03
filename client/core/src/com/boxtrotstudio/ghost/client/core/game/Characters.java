package com.boxtrotstudio.ghost.client.core.game;

public enum Characters {
    //TODO Replace this with character names

    GUN(0),
    LASER(1),
    CIRCLE(2),
    DASH(3),
    BOOMERANG(4),
    CONELAOE(7);

    byte id;
    Characters(int id) { this.id = (byte)id; }

    public byte getID() {
        return id;
    }

    public static Characters fromByte(byte b) {
        for (Characters c : values()) {
            if (c.getID() == b)
                return c;
        }

        return null;
    }
}
