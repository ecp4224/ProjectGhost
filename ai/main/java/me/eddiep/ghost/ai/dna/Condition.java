package me.eddiep.ghost.ai.dna;

import me.eddiep.ghost.game.match.entities.PlayableEntity;
import me.eddiep.ghost.utils.PFunction;

public interface Condition extends PFunction<PlayableEntity, Boolean> {

    ConditionType type();
}
