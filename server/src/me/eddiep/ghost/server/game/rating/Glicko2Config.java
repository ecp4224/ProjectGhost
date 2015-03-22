package me.eddiep.ghost.server.game.rating;

import me.eddiep.jconfig.system.Config;
import me.eddiep.jconfig.system.annotations.DefaultValue;
import me.eddiep.jconfig.system.annotations.Getter;

public interface Glicko2Config extends Config {
    @Getter(property = "tau")
    @DefaultValue(value = "0.5")
    public double getTau();

    @Getter(property = "defaultRating")
    @DefaultValue(value = "1500")
    public int getDefaultRating();

    @Getter(property = "ratingDeviation")
    @DefaultValue(value = "350")
    public int getDefaultRatingDeviation();

    @Getter(property = "defaultVolatility")
    @DefaultValue(value = "0.06")
    public double getDefaultVolatility();

    @Getter(property = "volatilityAlgorithm")
    @DefaultValue(value = "newprocedure")
    public String getVolatilityAlgorithm();
}
