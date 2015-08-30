package me.eddiep.ghost.common;

import me.eddiep.jconfig.system.Config;
import me.eddiep.jconfig.system.annotations.DefaultValue;
import me.eddiep.jconfig.system.annotations.Getter;

public interface BaseServerConfig extends Config {

    @Getter(property = "serverPort")
    @DefaultValue(value = "2546")
    public int getServerPort();

    @Getter(property = "serverMaxBacklog")
    @DefaultValue(value = "10000")
    public int getServerMaxBacklog();

    @Getter(property = "serverIp")
    @DefaultValue(value = "")
    public String getServerIP();
}
