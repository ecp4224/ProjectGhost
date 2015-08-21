package me.eddiep.ghost.game.match.stats;

public class Buff {

    public String name;
    public BuffType type;
    public double value;

    public Buff(String name, BuffType type, double value) {
        this.name = name;
        this.type = type;
        this.value = value;
    }

    protected boolean hasBuff(BuffType type) {
        return (this.type.id & type.id) != 0;
    }
}