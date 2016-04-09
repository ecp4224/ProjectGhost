package com.boxtrotstudio.updater;

import me.eddiep.jconfig.system.Config;
import me.eddiep.jconfig.system.annotations.Getter;
import me.eddiep.jconfig.system.annotations.Setter;

public interface ProgramConfig extends Config {

    @Getter(property = "name")
    String getName();

    @Getter(property = "execute")
    String execute();

    @Getter(property = "updateURL")
    String updateLocation();

    @Getter(property = "version")
    String currentVersion();

    @Setter(property = "version")
    void setCurrentVersion(String version);
}
