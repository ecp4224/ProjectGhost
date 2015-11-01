package me.eddiep.ghost.game.match.stats;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A Stat represents a numerical stat that can have temporary buffs applied to its value to alter the value returned.
 * The true value of a stat should not be changed unless it's considered a permanent change, as this will result in
 * unexpected results when many Buffs are applied.
 */

public final class Stat {

    private final String id;

    private boolean dirty = true;
    private double trueValue;
    private double cachedValue;

    private final Object valueLock = new Object();

    private final List<Buff> buffs = new ArrayList<>();

    public Stat(String id, Stat stat) {
        this.id = id;

        this.trueValue = stat.trueValue;
        this.buffs.addAll(stat.buffs);
    }

    public Stat(String id, double value) {
        this.id = id;

        this.trueValue = value;
    }

    public Stat(String id) {
        this(id, 0.0);
    }

    /**
     * Returns the network ID of this stat.<br />
     * This returns the ID without any checks. An ID should consist of exactly 4 bytes. When sending the stat update
     * packet, a shorter ID will be left-padded with spaces, but a longer one will throw an exception.
     */
    public String getId() {
        return id;
    }

    /**
     * Returns an unmodifiable list of the buffs applied to this stat.
     */
    public List<Buff> getBuffs() {
        return Collections.unmodifiableList(buffs);
    }

    /**
     * Adds a new buff to this stat.<br />
     *
     * Trying to add an unstackable buff that already exists will overwrite the value of the previous buff instead of
     * adding another one.
     *
     * @param name The name of the buff. Used for unstackable buffs.
     * @param type The type of the buff.
     * @param value The value of the buff.
     * @param stack Whether the buff is stackable or not. If a buff is stackable, then more than one can be applied and
     *              all will have an effect on the base value. If a buff is not stackable, then only one can be applied
     *              to a stat at a time.
     */
    public Buff addBuff(String name, BuffType type, double value, boolean stack) {
        dirty = true;

        if (!stack) {
            for (Buff buff: buffs) { //Java 7 makes me cry irl.
                if (buff.getName().equals(name)) {
                    buff.setType(type);
                    buff.setValue(value);

                    return buff;
                }
            }
        }

        Buff buff = new SimpleBuff(name, type, value);
        addBuff(buff);

        return buff;
    }

    /**
     * Adds a new buff to this stat that lasts for x seconds! <br />
     *
     * Trying to add an unstackable buff that already exists will overwrite the value of the previous buff instead of
     * adding another one.
     *
     * @param name The name of the buff. Used for unstackable buffs.
     * @param type The type of the buff.
     * @param value The value of the buff.
     * @param stack Whether the buff is stackable or not. If a buff is stackable, then more than one can be applied and
     *              all will have an effect on the base value. If a buff is not stackable, then only one can be applied
     *              to a stat at a time.
     * @param duration The duration, in seconds that indicates how long this buff lasts
     */
    public Buff addTimedBuff(String name, BuffType type, double value, boolean stack, double duration) {
        dirty = true;

        if (!stack) {
            for (Buff buff : buffs) { //Java 7 makes me cry irl.
                if (buff.getName().equals(name)) {
                    buff.setType(type);
                    buff.setValue(value);

                    return buff;
                }
            }
        }

        Buff buff = new TimedBuff(name, type, value, (long)(1000 * duration));
        addBuff(buff);

        return buff;
    }

    /**
     * Adds a stackable buff to this stat that expires in x seconds. Equivalent to {@code addBuff(name, type, value, true);}
     *
     * @param duration The duration, in seconds that indicates how long this buff lasts
     *
     * @see #addBuff(String, BuffType, double, boolean)
     */
    public Buff addTimedBuff(String name, BuffType type, double value, double duration) {
        return addTimedBuff(name, type, value, true, duration);
    }

    /**
     * Adds a stackable buff to this stat. Equivalent to {@code addBuff(name, type, value, true);}
     *
     * @see #addBuff(String, BuffType, double, boolean)
     */
    public Buff addBuff(String name, BuffType type, double value) {
        return addBuff(name, type, value, true);
    }

    public void removeBuff(SimpleBuff buff) {
        dirty = buffs.remove(buff);
    }

    public void removeBuff(String name) {
        for (Buff buff: buffs) { //Why Java7 why.
            if (buff.getName().equals(name)) {
                buffs.remove(buff);
                dirty = true;
                break;
            }
        }
    }

    public void addBuff(Buff buff) {
        buff.apply();
        buffs.add(buff);
    }

    /**
     * Removes all of the applied buffs.
     */
    public void removeBuffs() {
        buffs.clear();
        dirty = true;
    }

    /**
     * Returns the true value of this stat, without any buffs applied to it. Use the {@link #getValue()} method to get
     * the effective value of the buff.
     */
    public double getTrueValue() {
        return trueValue;
    }

    /**
     * Changes the true value of this stat. Unless the change is permanent, consider using {@link SimpleBuff buffs} instead.
     *
     * @param trueValue The new value of the buff.
     */
    public void setTrueValue(double trueValue) {
        this.trueValue = trueValue;
        dirty = true;
    }

    /**
     * Gets the value of this stat, with all the buffs applied to it. a cached value is used unless a modification has
     * occurred.
     */
    public double getValue() {
        synchronized (valueLock) {
            if (!dirty) {
                return cachedValue;
            }

            cachedValue = trueValue;

            for (Buff buff : buffs) {
                boolean percent = buff.hasBuff(BuffType.Percentage);

                if (buff.hasBuff(BuffType.Subtraction)) {
                    if (percent) {
                        cachedValue -= (buff.getValue() / 100.0) * cachedValue;
                    } else {
                        cachedValue -= buff.getValue();
                    }
                } else if (buff.hasBuff(BuffType.Addition)) {
                    if (percent) {
                        cachedValue += (buff.getValue() / 100.0) * cachedValue;
                    } else {
                        cachedValue += buff.getValue();
                    }
                }
            }

            dirty = false;
        }

        return cachedValue;
    }

    public boolean hasBuff(String name) {
        for (Buff buff : buffs) {
            if (buff.getName().equals(name))
                return true;
        }
        return false;
    }

    public void tick() {
        for (Buff buff : buffs) {
            buff.tick(this);
        }
    }
}
