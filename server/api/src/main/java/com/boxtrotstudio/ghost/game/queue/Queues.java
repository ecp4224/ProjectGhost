package com.boxtrotstudio.ghost.game.queue;

public enum Queues {
    //Ranked(0),
    /*Random2V2(1, RandomTwoVSTwoQueue.class, QueueType.RANDOM),
    Random1V1(2, RandomOneVSOneQueue.class, QueueType.RANDOM),
    Ranked1V1(3, RankedOneVSOneQueue.class, QueueType.RANKED),
    Casual1V1(4, CasualOneVSOneQueue.class, QueueType.CASUAL),
    Private(254, null, QueueType.PRIVATE),*/

    //DEMO QUEUES
    ORIGINAL(1,  QueueType.CASUAL),
    LASER(2, QueueType.CASUAL),
    WEAPONSELECT(3, QueueType.CASUAL),
    TWO_V_TWO(4, QueueType.CASUAL),
    DASH(5, QueueType.CASUAL),
    TUTORIAL(6, QueueType.CASUAL),
    BOOM(7, QueueType.CASUAL),

    RANKED(8, QueueType.RANKED),

    TEST(254, QueueType.CASUAL),
    UNKNOWN(255, QueueType.UNKNOWN);

    byte type;
    QueueType queueType;
    Queues(byte type, QueueType queueType) { this.queueType = queueType; this.type = type; }

    Queues(int i, QueueType queueType) {
        this.type = (byte) i;
        this.queueType = queueType;
    }

    public QueueType getQueueType() {

        return queueType;
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
