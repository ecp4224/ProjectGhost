package me.eddiep.ghost.game.match.stats;

public class SimpleBuff implements Buff {

    public String name;
    public BuffType type;
    public double value;

    public SimpleBuff(String name, BuffType type, double value) {
        this.name = name;
        this.type = type;
        this.value = value;
    }

    public boolean hasBuff(BuffType type) {
        return (this.type.id & type.id) != 0;
    }

    @Override
    public void apply() { }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public double getValue() {
        return value;
    }

    @Override
    public BuffType getType() {
        return type;
    }

    @Override
    public void tick(Stat owner) { }
}