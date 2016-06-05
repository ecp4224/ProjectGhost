package me.eddiep.ghost.ai.dna;

import com.boxtrotstudio.ghost.game.match.entities.PlayableEntity;

public interface Sequence<T> extends Comparable<Sequence<T>> {
    T execute(PlayableEntity owner);

    void mutate();

    double getWeignt();

    Sequence<T> combine(Sequence sequence);
}
