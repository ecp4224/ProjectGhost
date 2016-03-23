package me.eddiep.ghost.ai.dna.conditions;

import me.eddiep.ghost.ai.dna.Condition;
import me.eddiep.ghost.ai.dna.ConditionType;
import me.eddiep.ghost.utils.Global;

import java.util.ArrayList;

public class ConditionFactory {
    private static final Condition[] CONDITIONS = new Condition[] {
            new PlayableFiringCondition(),
            new PlayableFrozenCondition(),
            new PlayableInvisibleCondition(),
            new PlayableVisibleCondition(),
            new RandomCondition(),
            new EnemyTeamFiringCondition(),
            new EnemyTeamInvisibleCondition(),
            new EnemyTeamVisibleCondition(),
            new AllyTeamFiringCondition(),
            new AllyTeamInvisibleCondition(),
            new AllyTeamVisibleCondition()
    };

    public static Condition getRandomCondition(ConditionType type) {
        if (type == ConditionType.Any) {
            return CONDITIONS[Global.RANDOM.nextInt(CONDITIONS.length)];
        }

        ArrayList<Condition> matching = new ArrayList<>();
        for (Condition c : CONDITIONS) {
            if (c.type() == type || c.type() == ConditionType.Any) {
                matching.add(c);
            }
        }

        return matching.get(Global.RANDOM.nextInt(matching.size()));
    }

}
