package me.eddiep.ghost.server.game.queue;

import me.eddiep.ghost.server.game.queue.impl.RandomOneVSOneQueue;
import me.eddiep.ghost.server.game.queue.impl.RandomTwoVSTwoQueue;

public enum QueueType {
    //Ranked(0),
    Random2V2(1, RandomTwoVSTwoQueue.class),
    Random1V1(2, RandomOneVSOneQueue.class),
    UNKNOWN(255, null);
    //Private(2),
    //TwoVerusTwo(3);

    byte type;
    Class<? extends PlayerQueue> queueClass;
    PlayerQueue queue = null;
    QueueType(byte type, Class<? extends PlayerQueue> queueClass) { this.type = type; this.queueClass = queueClass; }

    QueueType(int i, Class<? extends PlayerQueue> queueClass) {
        this.type = (byte) i; this.queueClass = queueClass;
    }

    public Class<? extends PlayerQueue> getQueueClass() {
        return queueClass;
    }

    public PlayerQueue getQueue() {
        if (queueClass == null) return null;

        if (queue == null) {
            try {
                queue = queueClass.newInstance();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return queue;
    }

    public static QueueType byteToType(byte type) {
        for (QueueType t : values()) {
            if (t.type == type)
                return t;
        }
        return UNKNOWN;
    }

    public byte asByte() {
        return type;
    }
}
