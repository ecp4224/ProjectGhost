package me.eddiep.ghost.server.network.sql.impl;

import me.eddiep.jconfig.system.Config;
import me.eddiep.jconfig.system.annotations.DefaultValue;
import me.eddiep.jconfig.system.annotations.Getter;
import me.eddiep.jconfig.system.annotations.Setter;

public interface MongoConfig extends Config {

    @Getter(property = "ip")
    @DefaultValue(value = "127.0.0.1")
    String getIp();

    @Setter(property = "ip")
    void setIp(String ip);

    @Getter(property = "port")
    @DefaultValue(value = "27017")
    int getPort();

    @Setter(property = "port")
    void setPort(int port);

    @Getter(property = "database")
    @DefaultValue(value = "ghost")
    String getDatabaseName();

    @Setter(property = "database")
    void setDatabaseName(String name);
}
