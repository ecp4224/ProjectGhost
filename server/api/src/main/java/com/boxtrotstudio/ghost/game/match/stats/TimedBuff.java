package com.boxtrotstudio.ghost.game.match.stats;

public class TimedBuff extends SimpleBuff {

    private long startTime;
    private final long duration;
    public TimedBuff(String name, BuffType type, double value, long duration) {
        super(name, type, value);
        this.duration = duration;
    }

    @Override
    public void apply() {
        startTime = System.currentTimeMillis();
    }

    @Override
    public void tick(Stat owner) {
        if (System.currentTimeMillis() - startTime >= duration)
            owner.removeBuff(this);
    }
}
