package me.eddiep.ghost.ai.dna.conditions;

import com.boxtrotstudio.ghost.game.match.entities.PlayableEntity;
import me.eddiep.ghost.ai.dna.Condition;
import me.eddiep.ghost.ai.dna.ConditionType;

public class AllyTeamFiringCondition implements Condition {
    @Override
    public ConditionType type() {
        return ConditionType.Team;
    }

    @Override
    public Boolean run(PlayableEntity playableEntity) {
        for (PlayableEntity p : playableEntity.getAllies()) {
            if (p.isFiring())
                return true;
        }

        return false;
    }
}
