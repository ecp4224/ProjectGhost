package me.eddiep.ghost.server.game.queue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Queue;

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

    public List<Queues> getQueues() {
        ArrayList<Queues> toReturn = new ArrayList<>();
        for (Queues q : Queues.values()) {
            if (q.getQueueType() == this) {
                toReturn.add(q);
            }
        }
        return Collections.unmodifiableList(toReturn);
    }

    public static QueueType fromByte(byte val) {
        for (QueueType t : QueueType.values()) {
            if (t.type == val)
                return t;
        }

        return UNKNOWN;
    }
}
