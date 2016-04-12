package me.eddiep.ghost.ai.dna.fire;

import com.boxtrotstudio.ghost.game.match.entities.PlayableEntity;
import com.boxtrotstudio.ghost.game.match.entities.playable.impl.BaseNetworkPlayer;
import com.boxtrotstudio.ghost.utils.Global;
import com.boxtrotstudio.ghost.utils.Vector2f;
import me.eddiep.ghost.ai.dna.AbstractSequence;
import me.eddiep.ghost.ai.dna.Condition;
import me.eddiep.ghost.ai.dna.ConditionType;
import me.eddiep.ghost.ai.dna.Sequence;
import me.eddiep.ghost.ai.dna.conditions.ConditionFactory;

import java.util.HashMap;

public class PredictFiring extends AbstractSequence<Vector2f> {
    private HashMap<PlayableEntity, Vector2f> lastSeen = new HashMap<>();
    private HashMap<PlayableEntity, Vector2f> lastVelocity = new HashMap<>();
    private Condition condition;
    private float adder;

    public PredictFiring() {
        super();

        condition = ConditionFactory.getRandomCondition(ConditionType.Single);
        adder = Global.RANDOM.nextInt(100);
    }

    @Override
    public Vector2f execute(PlayableEntity owner) {
        updateLastSeen(owner);

        for (PlayableEntity key : lastSeen.keySet()) {
            if (condition.run(key)) {
                Vector2f vel = lastVelocity.get(key);
                Vector2f pos = lastSeen.get(key);
                double angle = Math.atan2(vel.y, vel.x);

                float x = (float) (pos.x + (Math.cos(angle) * vel.lengthSquared()));
                float y = (float) (pos.y + (Math.sin(angle) * vel.lengthSquared()));

                return new Vector2f(x, y);
            }
        }

        return null;
    }

    @Override
    public Sequence<Vector2f> combine(Sequence sequence) {
        if (sequence instanceof PredictFiring) {
            PredictFiring m = (PredictFiring) sequence;
            PredictFiring newMovement = new PredictFiring();

            if (Global.RANDOM.nextBoolean())
                newMovement.condition = condition;
            else
                newMovement.condition = m.condition;

            if (Global.RANDOM.nextBoolean())
                newMovement.weight = weight;
            else
                newMovement.weight = m.weight;

            if (Global.RANDOM.nextBoolean())
                newMovement.adder = adder;
            else
                newMovement.adder = m.adder;

            return newMovement;
        }

        return null;
    }

    private void updateLastSeen(PlayableEntity owner) {
        for (PlayableEntity e : owner.getOpponents()) {
            if (!lastSeen.containsKey(e)) {
                lastSeen.put(e, e.getPosition().cloneVector());
                lastVelocity.put(e, e.getVelocity().cloneVector());
            }
            if (e.isVisible()) {
                lastSeen.put(e, e.getPosition().cloneVector());
                lastVelocity.put(e, e.getVelocity().cloneVector());


                if (e instanceof BaseNetworkPlayer) {
                    System.out.println("Update last seen");
                }
            }
        }
    }

    @Override
    public void mutate() {
        int random = Global.random(0, 3);
        switch (random) {
            case 1:
                super.mutate();
                break;
            case 2:
                condition = ConditionFactory.getRandomCondition(ConditionType.Single);
                break;
            case 3:
                adder = Global.RANDOM.nextInt(100);
                break;
        }
    }

    @Override
    public String toString() {
        return "PredictFiring{" +
                "condition=" + condition.getClass().getSimpleName() +
                ", adder=" + adder +
                '}';
    }
}
