package me.eddiep.ghost.matchmaking.network.gameserver;

public class GameServerConfiguration {
    private long ID;
    private String internal_name;
    private byte queueServing;
    private String ip;
    private short port;

    private GameServerConfiguration() { }

    public byte getQueueServing() {
        return queueServing;
    }

    public String getInternalName() {
        return internal_name;
    }

    public long getID() {
        return ID;
    }

    public String getIp() { return ip; }

    public short getPort() { return port; }
}
