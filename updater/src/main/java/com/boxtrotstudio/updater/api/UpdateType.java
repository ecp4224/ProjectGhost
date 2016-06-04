package com.boxtrotstudio.updater.api;

public enum UpdateType {
    MAJOR(2),
    MINOR(1),
    BUGFIX(0),
    ROLLBACK(-1),
    UNKNOWN(-2),
    NEW(-3);

    int type;
    UpdateType(int type) { this.type = type; }

    public int getType() {
        return type;
    }

    public static UpdateType fromInt(int i) {
        for (UpdateType t : values()) {
            if (t.getType() == i)
                return t;
        }

        return UNKNOWN;
    }
}
