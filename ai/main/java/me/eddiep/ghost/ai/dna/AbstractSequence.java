package me.eddiep.ghost.ai.dna;


import com.boxtrotstudio.ghost.utils.Global;

public abstract class AbstractSequence<T> implements Sequence<T> {
    protected double weight;

    public AbstractSequence() {
        weight = Global.RANDOM.nextDouble();
    }

    @Override
    public void mutate() {
        if (Global.RANDOM.nextDouble() < 0.05) {
            weight = Global.RANDOM.nextDouble();
        }
    }

    @Override
    public double getWeignt() {
        return weight;
    }

    @Override
    public int compareTo(Sequence<T> o) {
        return getClass().getCanonicalName().compareTo(o.getClass().getCanonicalName());
    }
}
