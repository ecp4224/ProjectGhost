package com.boxtrotstudio.ghost.game.match.stats;

public interface Buff {

    /**
     * This method is invoked when this {@link Buff} object is applied to a stat
     */
    void apply();

    /**
     * The name of this {@link Buff}
     * @return The name
     */
    String getName();

    /**
     * The value of this {@link Buff}
     * @return The value
     */
    double getValue();

    /**
     * Change the type of this {@link Buff}
     * @param type The new {@link BuffType}
     */
    void setType(BuffType type);

    /**
     * Change the value of this {@link Buff}
     * @param value The new value
     */
    void setValue(double value);

    /**
     * The {@link BuffType} of this {@link Buff}
     * @return The {@link BuffType}
     */
    BuffType getType();

    /**
     * This method gets executed every world tick.
     * @param owner The {@link Stat} object that owns this {@link Buff}
     */
    void tick(Stat owner);

    /**
     * Whether this {@link Buff} has a {@link BuffType}
     * @param type The type to check for
     * @return Returns true if this {@link Buff} has the specified {@link BuffType}, otherwise false
     */
    boolean hasBuff(BuffType type);
}
