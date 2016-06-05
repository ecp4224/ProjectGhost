package me.eddiep.ghost.ai.dna.fire;

import com.boxtrotstudio.ghost.game.match.entities.PlayableEntity;
import com.boxtrotstudio.ghost.utils.Global;
import com.boxtrotstudio.ghost.utils.Vector2f;
import me.eddiep.ghost.ai.dna.AbstractSequence;
import me.eddiep.ghost.ai.dna.Condition;
import me.eddiep.ghost.ai.dna.ConditionType;
import me.eddiep.ghost.ai.dna.Sequence;
import me.eddiep.ghost.ai.dna.conditions.ConditionFactory;

import java.util.HashMap;

public class LastSeenFiring extends AbstractSequence<Vector2f> {
    private HashMap<PlayableEntity, Vector2f> lastSeen = new HashMap<>();
    private Condition condition;

    public LastSeenFiring() {
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
        int random = Global.random(0, 2);
        switch (random) {
            case 1:
                super.mutate();
                break;
            case 2:
                condition = ConditionFactory.getRandomCondition(ConditionType.Single);
                break;
        }
    }

    @Override
    public Sequence<Vector2f> combine(Sequence sequence) {
        if (sequence instanceof LastSeenFiring) {
            LastSeenFiring m = (LastSeenFiring)sequence;
            LastSeenFiring newMovement = new LastSeenFiring();

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
            if (!lastSeen.containsKey(e)) {
                lastSeen.put(e, e.getPosition().cloneVector());
            }
            if (e.isVisible()) {
                lastSeen.put(e, e.getPosition().cloneVector());
            }
        }
    }

    @Override
    public String toString() {
        return "LastSeenFiring{" +
                "condition=" + condition.getClass().getSimpleName() +
                '}';
    }
}
