package me.eddiep.ghost.server.network.queue;

public enum QueueType {
    Ranked(0),
    Random(1),
    Private(2),
    TwoVerusTwo(3);

    byte type;
    QueueType(byte type) { this.type = type; }

    QueueType(int i) {
        this.type = (byte) i;
    }
}
