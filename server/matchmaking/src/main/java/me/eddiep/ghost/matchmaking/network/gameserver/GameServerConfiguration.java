package me.eddiep.ghost.matchmaking.network.gameserver;

public class GameServerConfiguration {
    private String internal_name;
    private int streamLevel;
    private String ip;
    private short port;

    private GameServerConfiguration() { }

    public String getInternalName() {
        return internal_name;
    }

    public String getIp() { return ip; }

    public short getPort() { return port; }

    public Stream getStream() {
        return Stream.fromInt(streamLevel);
    }
}
