package me.eddiep.ghost.ai.dna.conditions;

import me.eddiep.ghost.ai.dna.Condition;
import me.eddiep.ghost.ai.dna.ConditionType;
import me.eddiep.ghost.game.match.entities.PlayableEntity;

public class PlayableFiringCondition implements Condition {

    @Override
    public ConditionType type() {
        return ConditionType.Single;
    }

    @Override
    public Boolean run(PlayableEntity enemy) {
        return enemy.isFiring();
    }
}
