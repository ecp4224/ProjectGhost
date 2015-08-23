package me.eddiep.ghost.gameserver.api;

import me.eddiep.jconfig.system.Config;
import me.eddiep.jconfig.system.annotations.DefaultValue;
import me.eddiep.jconfig.system.annotations.Getter;

public interface GameServerConfig extends Config {

    @Getter(property = "matchmakingIP")
    @DefaultValue(value = "127.0.0.1")
    public String matchmakingIP();

    @Getter(property = "matchmakingPort")
    @DefaultValue(value = "2178")
    public int matchmakingPort();

    @Getter(property = "matchmakingSecret")
    @DefaultValue(value = "super_secret_12345")
    public String matchmakingSecret();

    @Getter(property = "serverID")
    @DefaultValue(value = "1")
    public long ID();

    @Getter(property = "serverPort")
    @DefaultValue(value = "2179")
    public int getServerPort();

    @Getter(property = "serverMaxBacklog")
    @DefaultValue(value = "10000")
    public int getServerMaxBacklog();

    @Getter(property = "serverIp")
    @DefaultValue(value = "")
    public String getServerIP();

    @Getter(property = "maxMatchCount")
    @DefaultValue(value = "50")
    public short getMaxMatchCount();

    @Getter(property = "heartbeatInterval")
    @DefaultValue(value = "300")
    public long getHeartbeatInterval();
}
