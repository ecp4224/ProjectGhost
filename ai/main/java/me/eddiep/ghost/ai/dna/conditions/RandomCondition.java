package me.eddiep.ghost.ai.dna.conditions;

import me.eddiep.ghost.ai.dna.Condition;
import me.eddiep.ghost.ai.dna.ConditionType;
import com.boxtrotstudio.ghost.game.match.entities.PlayableEntity;
import com.boxtrotstudio.ghost.utils.Global;

public class RandomCondition implements Condition {

    @Override
    public ConditionType type() {
        return ConditionType.Any;
    }

    @Override
    public Boolean run(PlayableEntity playableEntity) {
        return Global.RANDOM.nextBoolean();
    }
}
