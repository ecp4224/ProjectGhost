package me.eddiep.ghost.ai.dna.movement;

import me.eddiep.ghost.ai.dna.AbstractSequence;
import me.eddiep.ghost.ai.dna.Condition;
import me.eddiep.ghost.ai.dna.ConditionType;
import me.eddiep.ghost.ai.dna.Sequence;
import me.eddiep.ghost.ai.dna.conditions.ConditionFactory;
import me.eddiep.ghost.game.match.entities.PlayableEntity;
import me.eddiep.ghost.utils.Global;
import me.eddiep.ghost.utils.Vector2f;

import java.util.HashMap;

public class SeekMovement extends AbstractSequence<Vector2f> {
    private HashMap<PlayableEntity, Vector2f> lastSeen = new HashMap<>();
    private Condition condition;

    public SeekMovement() {
        super();

        condition = ConditionFactory.getRandomCondition(ConditionType.Single);
    }

    @Override
    public Vector2f execute(PlayableEntity owner) {
        updateLastSeen(owner);

        for (PlayableEntity key : lastSeen.keySet()) {
            if (condition.run(key)) {
                return lastSeen.get(key); //This will seek directly to them
            }
        }

        return null;
    }

    @Override
    public void mutate() {
        super.mutate();

        condition = ConditionFactory.getRandomCondition(ConditionType.Single);
    }

    @Override
    public Sequence combine(Sequence sequence) {
        if (sequence instanceof SeekMovement) {
            SeekMovement m = (SeekMovement)sequence;
            SeekMovement newMovement = new SeekMovement();

            if (Global.RANDOM.nextBoolean())
                newMovement.condition = condition;
            else
                newMovement.condition = m.condition;

            if (Global.RANDOM.nextBoolean())
                newMovement.weight = weight;
            else
                newMovement.weight = m.weight;

            return newMovement;
        }

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
