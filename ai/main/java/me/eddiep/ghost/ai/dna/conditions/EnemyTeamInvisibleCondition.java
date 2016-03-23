package me.eddiep.ghost.ai.dna.conditions;

import me.eddiep.ghost.game.match.entities.PlayableEntity;

public class EnemyTeamInvisibleCondition extends EnemyTeamVisibleCondition {

    @Override
    public Boolean run(PlayableEntity playableEntity) {
        return !super.run(playableEntity);
    }
}
