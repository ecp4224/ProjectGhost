package me.eddiep.ghost.server.game.queue;

public enum QueueType {
    RANKED(0),
    CASUAL(1),
    RANDOM(2),
    PRIVATE(3),
    UNKNOWN(255);

    byte type;
    QueueType(byte type) { this.type = type; }
    QueueType(int type) { this.type = (byte) type; }

    public byte asByte() {
        return type;
    }

    public static QueueType fromByte(byte val) {
        for (QueueType t : QueueType.values()) {
            if (t.type == val)
                return t;
        }

        return UNKNOWN;
    }
}
