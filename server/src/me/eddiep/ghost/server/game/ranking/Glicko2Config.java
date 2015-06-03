package me.eddiep.ghost.server.game.ranking;

import me.eddiep.jconfig.system.Config;
import me.eddiep.jconfig.system.annotations.DefaultValue;
import me.eddiep.jconfig.system.annotations.Getter;
import me.eddiep.jconfig.system.annotations.Setter;

public interface Glicko2Config extends Config {
    @Getter(property = "tau")
    @DefaultValue(value = "0.5")
    double getTau();

    @Getter(property = "defaultRating")
    @DefaultValue(value = "1500")
    int getDefaultRating();

    @Getter(property = "ratingDeviation")
    @DefaultValue(value = "350")
    int getDefaultRatingDeviation();

    @Getter(property = "defaultVolatility")
    @DefaultValue(value = "0.06")
    double getDefaultVolatility();

    @Getter(property = "volatilityAlgorithm")
    @DefaultValue(value = "newprocedure")
    String getVolatilityAlgorithm();

    @Getter(property = "rankUpdateCap")
    @DefaultValue(value = "500")
    int getUpdateCap();

    @Getter(property = "rankUpdateTime")
    @DefaultValue(value = "86400000")
    int getUpdateTime();

    @Getter(property = "lastUpdate")
    @DefaultValue(value = "0")
    long getLastUpdateTime();

    @Setter(property = "lastUpdate")
    void setLastUpdateTime(long time);
}
