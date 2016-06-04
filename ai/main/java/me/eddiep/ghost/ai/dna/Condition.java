package me.eddiep.ghost.ai.dna;

import com.boxtrotstudio.ghost.game.match.entities.PlayableEntity;
import com.boxtrotstudio.ghost.utils.PFunction;

public interface Condition extends PFunction<PlayableEntity, Boolean> {

    ConditionType type();
}
