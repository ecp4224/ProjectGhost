package me.eddiep.ghost.server;

import me.eddiep.jconfig.system.Config;
import me.eddiep.jconfig.system.annotations.DefaultValue;
import me.eddiep.jconfig.system.annotations.Getter;

public interface ServerConfig extends Config {

    @Getter(property = "udpPort")
    @DefaultValue(value = "4380")
    public int getUDPBindPort();

    @Getter(property = "tcpPort")
    @DefaultValue(value = "4379")
    public int getTCPBindPort();

    @Getter(property = "centralServerSecret")
    @DefaultValue(value = "REPLACE_THIS_SECRET")
    public String getCentralServerSecret();
}
