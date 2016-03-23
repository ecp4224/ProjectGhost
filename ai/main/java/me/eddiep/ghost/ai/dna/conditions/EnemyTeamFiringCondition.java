package me.eddiep.ghost.ai.dna.conditions;

import me.eddiep.ghost.ai.dna.Condition;
import me.eddiep.ghost.ai.dna.ConditionType;
import me.eddiep.ghost.game.match.entities.PlayableEntity;

public class EnemyTeamFiringCondition implements Condition {
    @Override
    public ConditionType type() {
        return ConditionType.Team;
    }

    @Override
    public Boolean run(PlayableEntity playableEntity) {
        for (PlayableEntity e : playableEntity.getOpponents()) {
            if (e.isFiring())
                return true;
        }

        return false;
    }
}
