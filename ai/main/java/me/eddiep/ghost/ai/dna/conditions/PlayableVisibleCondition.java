package me.eddiep.ghost.ai.dna.conditions;

import me.eddiep.ghost.ai.dna.Condition;
import me.eddiep.ghost.ai.dna.ConditionType;
import me.eddiep.ghost.game.match.entities.PlayableEntity;

public class PlayableVisibleCondition implements Condition {

    @Override
    public Boolean run(PlayableEntity enemy) {
        return enemy.isVisible();
    }

    @Override
    public ConditionType type() {
        return ConditionType.Single;
    }
}
