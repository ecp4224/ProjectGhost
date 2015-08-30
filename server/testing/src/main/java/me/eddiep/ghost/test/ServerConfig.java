package me.eddiep.ghost.test;

import me.eddiep.ghost.common.BaseServerConfig;
import me.eddiep.jconfig.system.annotations.DefaultValue;
import me.eddiep.jconfig.system.annotations.Getter;

public interface ServerConfig extends BaseServerConfig {

    @Getter(property = "sqlDriver")
    @DefaultValue(value = "me.eddiep.ghost.test.network.sql.impl.MongoDB")
    String getSQLDriver();
}
