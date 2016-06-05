package me.eddiep.ghost.ai.dna.movement;

import com.boxtrotstudio.ghost.game.match.entities.PlayableEntity;
import com.boxtrotstudio.ghost.utils.Global;
import com.boxtrotstudio.ghost.utils.Vector2f;
import me.eddiep.ghost.ai.dna.AbstractSequence;
import me.eddiep.ghost.ai.dna.Condition;
import me.eddiep.ghost.ai.dna.ConditionType;
import me.eddiep.ghost.ai.dna.Sequence;
import me.eddiep.ghost.ai.dna.conditions.ConditionFactory;

import java.util.HashMap;

public class AvoidMovement extends AbstractSequence<Vector2f> {
    private HashMap<PlayableEntity, Vector2f> lastSeen = new HashMap<>();
    private HashMap<PlayableEntity, Vector2f> lastSeenMovement = new HashMap<>();

    private Condition condition;
    private Condition avoidWho;

    public AvoidMovement() {
        super();

        condition = ConditionFactory.getRandomCondition(ConditionType.Any);
        avoidWho = ConditionFactory.getRandomCondition(ConditionType.Single);
    }

    @Override
    public Vector2f execute(PlayableEntity owner) {
        updateLastSeen(owner);

        if (condition.run(owner)) {
            for (PlayableEntity key : lastSeen.keySet()) {
                if (avoidWho.run(key)) {
                    Vector2f lastMovement = lastSeenMovement.get(key);
                    if (lastMovement.length() == 0f) {
                        continue; //We can't avoid someone who isn't moving
                    }
                    double angle = Math.toDegrees(Math.atan2(lastMovement.y, lastMovement.x));

                    angle += 90;

                    angle = Math.toRadians(angle);

                    float x = (float) (owner.getX() + (200f * Math.cos(angle)));
                    float y = (float) (owner.getY() + (200f * Math.sin(angle)));

                    x = Math.min(Math.max(x, 0), owner.getMatch().getUpperBounds().x);
                    y = Math.min(Math.max(y, 0), 720);

                    return new Vector2f(x, y);
                }
            }
        }

        return null;
    }

    @Override
    public Sequence<Vector2f> combine(Sequence sequence) {
        if (sequence instanceof AvoidMovement) {
            AvoidMovement avoid = (AvoidMovement)sequence;

            AvoidMovement newMovement = new AvoidMovement();

            if (Global.RANDOM.nextBoolean())
                newMovement.condition = condition;
            else
                newMovement.condition = avoid.condition;

            if (Global.RANDOM.nextBoolean())
                newMovement.avoidWho = avoidWho;
            else
                newMovement.avoidWho = avoid.avoidWho;

            if (Global.RANDOM.nextBoolean())
                newMovement.weight = weight;
            else
                newMovement.weight = avoid.weight;

            return newMovement;
        }
        return null;
    }

    @Override
    public void mutate() {
        int temp = Global.random(0, 3);
        switch (temp) {
            case 0:
                condition = ConditionFactory.getRandomCondition(ConditionType.Any);
                break;
            case 1:
                avoidWho = ConditionFactory.getRandomCondition(ConditionType.Single);
                break;
            case 2:
                super.mutate();
                break;
        }
    }

    private void updateLastSeen(PlayableEntity owner) {
        for (PlayableEntity e : owner.getOpponents()) {
            if (!lastSeen.containsKey(e)) {
                lastSeen.put(e, e.getPosition().cloneVector());
                lastSeenMovement.put(e, e.getVelocity().cloneVector());
            }
            if (e.isVisible()) {
                lastSeen.put(e, e.getPosition().cloneVector());
                lastSeenMovement.put(e, e.getVelocity().cloneVector());
            }
        }
    }

    @Override
    public String toString() {
        return "AvoidMovement{" +
                "condition=" + condition.getClass().getSimpleName() +
                ", avoidWho=" + avoidWho.getClass().getSimpleName() +
                '}';
    }
}
