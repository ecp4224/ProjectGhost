package com.boxtrotstudio.ghost.client.core.game;

import java.io.File;

public enum Characters {
    //TODO Replace this with character names

    GUN(0, "characters/gun.json"),
    LASER(1, "characters/laser.json"),
    CIRCLE(2, "characters/circle.json"),
    DASH(3, "characters/dash.json"),
    BOOMERANG(4, "characters/boomerang.json"),
    CONELAOE(7, "characters/conelaoe.json");

    String characterFile;
    byte id;
    Characters(int id, String path) { this.id = (byte)id; this.characterFile = path; }

    public byte getID() {
        return id;
    }

    public File getCharacterFile() {
        return new File(characterFile);
    }

    public static Characters fromByte(byte b) {
        for (Characters c : values()) {
            if (c.getID() == b)
                return c;
        }

        return null;
    }
}
