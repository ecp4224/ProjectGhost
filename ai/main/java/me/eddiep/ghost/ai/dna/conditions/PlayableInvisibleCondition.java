package me.eddiep.ghost.ai.dna.conditions;

import me.eddiep.ghost.game.match.entities.PlayableEntity;

public class PlayableInvisibleCondition extends PlayableVisibleCondition {

    @Override
    public Boolean run(PlayableEntity enemy) {
        return !super.run(enemy);
    }
}
