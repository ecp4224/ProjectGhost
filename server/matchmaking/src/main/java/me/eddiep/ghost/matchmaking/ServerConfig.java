package me.eddiep.ghost.matchmaking;

import me.eddiep.jconfig.system.Config;
import me.eddiep.jconfig.system.annotations.DefaultValue;
import me.eddiep.jconfig.system.annotations.Getter;

public interface ServerConfig extends Config {

    @Getter(property = "loginServerIP")
    @DefaultValue(value = "127.0.0.1")
    public String getLoginServerIP();

    @Getter(property = "loginServerPort")
    @DefaultValue(value = "80")
    public int getLoginServerPort();

    @Getter(property = "serverPort")
    @DefaultValue(value = "2178")
    public int getServerPort();

    @Getter(property = "serverMaxBacklog")
    @DefaultValue(value = "10000")
    public int getServerMaxBacklog();

    @Getter(property = "serverIp")
    @DefaultValue(value = "")
    public String getServerIP();

    @Getter(property = "secret")
    @DefaultValue(value = "super_secret_12345")
    public String getServerSecret();

    @Getter(property = "adminSecret")
    @DefaultValue(value = "super_admin_1221")
    public String getAdminSecret();
}
