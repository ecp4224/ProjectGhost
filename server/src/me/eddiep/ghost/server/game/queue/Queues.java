package me.eddiep.ghost.server.game.queue;

import me.eddiep.ghost.server.game.queue.impl.OriginalQueue;

public enum Queues {
    //Ranked(0),
    /*Random2V2(1, RandomTwoVSTwoQueue.class, QueueType.RANDOM),
    Random1V1(2, RandomOneVSOneQueue.class, QueueType.RANDOM),
    Ranked1V1(3, RankedOneVSOneQueue.class, QueueType.RANKED),
    Casual1V1(4, CasualOneVSOneQueue.class, QueueType.CASUAL),
    Private(254, null, QueueType.PRIVATE),*/

    //DEMO QUEUES
    ORIGINAL(1, OriginalQueue.class, QueueType.CASUAL),

    UNKNOWN(255, null, QueueType.UNKNOWN);

    byte type;
    Class<? extends PlayerQueue> queueClass;
    PlayerQueue queue = null;
    QueueType queueType;
    Queues(byte type, Class<? extends PlayerQueue> queueClass, QueueType queueType) { this.queueType = queueType; this.type = type; this.queueClass = queueClass; }

    Queues(int i, Class<? extends PlayerQueue> queueClass, QueueType queueType) {
        this.type = (byte) i; this.queueClass = queueClass;
        this.queueType = queueType;
    }

    public Class<? extends PlayerQueue> getQueueClass() {
        return queueClass;
    }

    public QueueType getQueueType() {
        return queueType;
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

    public boolean isRanked() {
        return queueType == QueueType.RANKED;
    }

    public static Queues byteToType(byte type) {
        for (Queues t : values()) {
            if (t.type == type)
                return t;
        }
        return UNKNOWN;
    }

    public byte asByte() {
        return type;
    }

    public static Queues nameToType(String queue) {
        for (Queues t : values()) {
            if (t.name().equalsIgnoreCase(queue))
                return t;
        }
        return UNKNOWN;
    }
}
