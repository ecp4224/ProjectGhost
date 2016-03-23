package me.eddiep.ghost.ai.dna;

import me.eddiep.ghost.game.match.entities.PlayableEntity;

public interface Sequence<T> {
    T execute(PlayableEntity owner);

    void mutate();

    double getWeignt();

    Sequence combine(Sequence sequence);
}
