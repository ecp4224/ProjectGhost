package me.eddiep.ghost.ai.dna.movement;

import com.boxtrotstudio.ghost.game.match.entities.PlayableEntity;
import com.boxtrotstudio.ghost.utils.Vector2f;
import me.eddiep.ghost.ai.dna.AbstractSequence;
import me.eddiep.ghost.ai.dna.Condition;
import me.eddiep.ghost.ai.dna.ConditionType;
import me.eddiep.ghost.ai.dna.Sequence;
import me.eddiep.ghost.ai.dna.conditions.ConditionFactory;

import java.util.HashMap;

public class AvoidMovement extends AbstractSequence<PlayableEntity> {
    private HashMap<PlayableEntity, Vector2f> lastSeen = new HashMap<>();

    private Condition condition;

    public AvoidMovement() {
        super();

        condition = ConditionFactory.getRandomCondition(ConditionType.Any);
    }

    @Override
    public PlayableEntity execute(PlayableEntity owner) {
        updateLastSeen(owner);

        if (condition.run(owner)) {

        }

        return null;
    }

    @Override
    public Sequence combine(Sequence sequence) {
        return null;
    }

    private void updateLastSeen(PlayableEntity owner) {
        for (PlayableEntity e : owner.getOpponents()) {
            if (e.isVisible()) {
                lastSeen.put(e, e.getPosition());
            }
        }
    }
}
