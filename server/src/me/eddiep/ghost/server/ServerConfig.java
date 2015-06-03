package me.eddiep.ghost.server;

import me.eddiep.jconfig.system.Config;
import me.eddiep.jconfig.system.annotations.DefaultValue;
import me.eddiep.jconfig.system.annotations.Getter;

public interface ServerConfig extends Config {

    @Getter(property = "sqlDriver")
    @DefaultValue(value = "me.eddiep.ghost.server.network.sql.impl.MongoDB")
    String getSQLDriver();
}
