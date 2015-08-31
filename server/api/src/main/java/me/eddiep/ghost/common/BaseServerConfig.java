package me.eddiep.ghost.common;

import me.eddiep.jconfig.system.Config;
import me.eddiep.jconfig.system.annotations.DefaultValue;
import me.eddiep.jconfig.system.annotations.Getter;

public interface BaseServerConfig extends Config {

    @Getter(property = "serverPort")
    @DefaultValue(value = "2546")
    int getServerPort();

    @Getter(property = "serverMaxBacklog")
    @DefaultValue(value = "10000")
    int getServerMaxBacklog();

    @Getter(property = "serverIp")
    @DefaultValue(value = "")
    String getServerIP();

    @Getter(property = "tickGroupSize")
    @DefaultValue(value = "10")
    int getTickGroupSize();
}
