package com.boxtrotstudio.ghost.matchmaking.queue;

import com.boxtrotstudio.ghost.game.queue.Queues;

public class QueueInfo {
    private long playersInQueue;
    private long playersInMatch;
    private byte type;
    private String name;
    private String description;
    private int allyCount;
    private int opponentCount;

    public QueueInfo(Queues type, long playersInQueue, long playersInMatch, String description, int allyCount, int opponentCount) {
        this.type = type.asByte();
        this.name = type.name();
        this.playersInMatch = playersInMatch;
        this.playersInQueue = playersInQueue;
        this.description = description;
        this.allyCount = allyCount;
        this.opponentCount = opponentCount;
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

    public int getAllyCount() {
        return allyCount;
    }

    public int getOpponentCount() {
        return opponentCount;
    }
}
