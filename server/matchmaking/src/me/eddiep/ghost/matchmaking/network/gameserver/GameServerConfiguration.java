package me.eddiep.ghost.matchmaking.network.gameserver;

public class GameServerConfiguration {
    private long ID;
    private String internal_name;
    private byte queueServing;

    private GameServerConfiguration() { }

    public byte getQueueServing() {
        return queueServing;
    }

    public String getInternal_name() {
        return internal_name;
    }

    public long getID() {
        return ID;
    }
}
