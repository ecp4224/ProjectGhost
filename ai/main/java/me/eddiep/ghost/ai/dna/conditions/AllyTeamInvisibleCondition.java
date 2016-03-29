package me.eddiep.ghost.ai.dna.conditions;

import me.eddiep.ghost.game.match.entities.PlayableEntity;

public class AllyTeamInvisibleCondition extends AllyTeamVisibleCondition {

    @Override
    public Boolean run(PlayableEntity owner) {
        return !super.run(owner);
    }
}