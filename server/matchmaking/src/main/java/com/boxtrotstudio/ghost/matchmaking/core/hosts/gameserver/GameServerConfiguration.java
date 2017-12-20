package com.boxtrotstudio.ghost.matchmaking.core.hosts.gameserver;

public class GameServerConfiguration {
    private String internal_group = "Default";
    private int streamLevel;

    private GameServerConfiguration() { }

    public String getInternalGroup() {
        return internal_group;
    }

    public Stream getStream() {
        return Stream.fromInt(streamLevel);
    }

    public void setStream(Stream stream) {
        this.streamLevel = stream.getLevel();
    }
}
