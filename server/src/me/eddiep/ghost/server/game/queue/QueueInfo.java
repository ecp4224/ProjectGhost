package me.eddiep.ghost.server.game.queue;

public class QueueInfo {
    private long playersInQueue;
    private long playersInMatch;
    private byte type;
    private String name;
    private String description;
    private boolean isRanked;

    public QueueInfo(QueueType type, long playersInQueue, long playersInMatch, String description, boolean isRanked) {
        this.type = type.asByte();
        this.name = type.name();
        this.playersInMatch = playersInMatch;
        this.playersInQueue = playersInQueue;
        this.description = description;
        this.isRanked = isRanked;
    }

    public boolean isRanked() {
        return isRanked;
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
