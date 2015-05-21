package me.eddiep.ghost.central.network.gameserv;


public class QueueInfo {
    private long playersInQueue;
    private long playersInMatch;
    private byte type;
    private String name;
    private String description;
    private int allyCount;
    private int opponentCount;
    private boolean isRanked;

    private QueueInfo() { }

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

    public int getAllyCount() {
        return allyCount;
    }

    public int getOpponentCount() {
        return opponentCount;
    }
}
