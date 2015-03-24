package me.eddiep.ghost.server.game.queue;

public class QueueInfo {
    private long playersInQueue;
    private long playersInMatch;
    private byte type;
    private String name;
    private String description;

    public QueueInfo(Queues type, long playersInQueue, long playersInMatch, String description) {
        this.type = type.asByte();
        this.name = type.name();
        this.playersInMatch = playersInMatch;
        this.playersInQueue = playersInQueue;
        this.description = description;
    }

    public boolean isRanked() {
        return Queues.byteToType(type).isRanked();
    }

    public String getDescription() {
        return description;
    }

    public long getPlayersInMatch() {
        return playersInMatch;
    }

    public long getPlayersInQueue() {
        return playersInQueue;
    }

    public byte getType() {
        return type;
    }

    public String getName() {
        return name;
    }
}
