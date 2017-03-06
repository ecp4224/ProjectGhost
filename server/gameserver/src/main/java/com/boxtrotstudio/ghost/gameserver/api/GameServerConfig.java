package com.boxtrotstudio.ghost.gameserver.api;

import com.boxtrotstudio.ghost.common.BaseServerConfig;
import me.eddiep.jconfig.system.annotations.DefaultValue;
import me.eddiep.jconfig.system.annotations.Getter;

public interface GameServerConfig extends BaseServerConfig {

    @Getter(property = "matchmakingIP")
    @DefaultValue(value = "127.0.0.1")
    String matchmakingIP();

    @Getter(property = "matchmakingPort")
    @DefaultValue(value = "2178")
    int matchmakingPort();

    @Getter(property = "matchmakingSecret")
    @DefaultValue(value = "super_secret_12345")
    String matchmakingSecret();

    @Getter(property = "serverID")
    @DefaultValue(value = "1")
    long ID();

    @Getter(property = "maxMatchCount")
    @DefaultValue(value = "50")
    short getMaxMatchCount();

    @Getter(property = "heartbeatInterval")
    @DefaultValue(value = "300")
    long getHeartbeatInterval();

    @Getter(property = "versionUrl")
    @DefaultValue(value = "https://downloads.boxtrotstudio.com/ghost/version.txt")
    String getVersionURL();

    @Getter(property = "versionFile")
    @DefaultValue(value = ".version")
    String getVersionFile();
}
