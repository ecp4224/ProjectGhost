package me.eddiep.ghost.server.network.sql.impl;

import me.eddiep.jconfig.system.Config;
import me.eddiep.jconfig.system.annotations.DefaultValue;
import me.eddiep.jconfig.system.annotations.Getter;
import me.eddiep.jconfig.system.annotations.Setter;

public interface MongoConfig extends Config {

    @Getter(property = "ip")
    @DefaultValue(value = "127.0.0.1")
    public String getIp();

    @Setter(property = "ip")
    public void setIp(String ip);

    @Getter(property = "port")
    @DefaultValue(value = "27017")
    public int getPort();

    @Setter(property = "port")
    public void setPort(int port);

    @Getter(property = "database")
    @DefaultValue(value = "ghost")
    public String getDatabaseName();

    @Setter(property = "database")
    public void setDatabaseName(String name);
}
