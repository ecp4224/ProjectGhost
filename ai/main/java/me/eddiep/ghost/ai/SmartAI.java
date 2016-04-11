package me.eddiep.ghost.ai;

import com.boxtrotstudio.ghost.game.match.Match;
import com.boxtrotstudio.ghost.game.match.entities.PlayableEntity;
import com.boxtrotstudio.ghost.game.match.entities.playable.BasePlayableEntity;
import com.boxtrotstudio.ghost.game.match.stats.TemporaryStats;
import com.boxtrotstudio.ghost.utils.Global;
import com.boxtrotstudio.ghost.utils.Vector2f;
import me.eddiep.ghost.ai.dna.Sequence;
import me.eddiep.ghost.ai.dna.fire.LastSeenFiring;
import me.eddiep.ghost.ai.dna.fire.RandomFiring;
import me.eddiep.ghost.ai.dna.movement.AvoidMovement;
import me.eddiep.ghost.ai.dna.movement.RandomMovement;
import me.eddiep.ghost.ai.dna.movement.SeekMovement;
import org.encog.neural.networks.BasicNetwork;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class SmartAI extends BasePlayableEntity {
    private List<Sequence<Vector2f>> movementDNA = new ArrayList<>();
    private List<Sequence<Vector2f>> fireDNA = new ArrayList<>();

    public SmartAI() {
        for (int i = 0; i < 4; i++) {
            int select = Global.random(0, 3);
            switch (select) {
                case 0:
                    movementDNA.add(new RandomMovement());
                    break;
                case 1:
                    movementDNA.add(new AvoidMovement());
                    break;
                case 2:
                    movementDNA.add(new SeekMovement());
            }
        }

        for (int i = 0; i < 4; i++) {
            fireDNA.add(Global.RANDOM.nextBoolean() ? new LastSeenFiring() : new RandomFiring());
        }

        setName("SMART_BOT");
        setReady(true);
    }

    public SmartAI(List<Sequence<Vector2f>> movementDNA, List<Sequence<Vector2f>> fire) {
        this.movementDNA = movementDNA;
        this.fireDNA = fire;

        setName("SMART_BOT");
        setReady(true);
    }

    public void onWin(Match match) { }

    public void onLose(Match match) { }

    public void onDamagePlayable(PlayableEntity hit) { }

    public void onShotMissed() { }

    public TemporaryStats getCurrentMatchStats() {
        return null;
    }

    public SmartAI mateWith(SmartAI ai) {
        Collections.sort(movementDNA);
        Collections.sort(ai.movementDNA);
        ArrayList<Sequence<Vector2f>> movementNewDNA = new ArrayList<>();

        for (int i = 0; i < movementDNA.size(); i++) {
            Sequence<Vector2f> ours = movementDNA.get(i);
            Sequence<Vector2f> theirs = ai.movementDNA.get(i);

            Sequence<Vector2f> mate = ours.combine(theirs);
            if (mate == null) {
                mate = Global.RANDOM.nextBoolean() ? ours : theirs;
                mate.mutate();
            } else if (Global.RANDOM.nextDouble() < 0.03) {
                mate.mutate();
            }

            movementNewDNA.add(mate);
        }

        ArrayList<Sequence<Vector2f>> fireDNA = new ArrayList<>();

        for (int i = 0; i < fireDNA.size(); i++) {
            Sequence<Vector2f> ours = fireDNA.get(i);
            Sequence<Vector2f> theirs = ai.fireDNA.get(i);

            Sequence<Vector2f> mate = ours.combine(theirs);
            if (mate == null) {
                mate = Global.RANDOM.nextBoolean() ? ours : theirs;
                mate.mutate();
            } else if (Global.RANDOM.nextDouble() < 0.03) {
                mate.mutate();
            }

            fireDNA.add(mate);
        }

        return new SmartAI(movementNewDNA, fireDNA);
    }

    @Override
    public void onDamage(PlayableEntity damager) {
        super.onDamage(damager);
    }

    @Override
    public void tick() {
        if (getTarget() == null) {
            Vector2f[] results = new Vector2f[movementDNA.size()];
            int resultCount = 0;
            for (int i = 0; i < results.length; i++) {
                results[i] = movementDNA.get(i).execute(this);

                if (results[i] != null)
                    resultCount++;
            }

            float x = 0f;
            float y = 0f;
            if (resultCount > 1) {
                //System.out.println(resultCount + " results");
                float weightSum = 0f;
                for (int i = 0; i < results.length; i++) {
                    if (results[i] != null) {
                        weightSum += movementDNA.get(i).getWeignt();
                        x += (movementDNA.get(i).getWeignt() * results[i].getX());
                        y += (movementDNA.get(i).getWeignt() * results[i].getY());
                    }
                }

                x /= weightSum;
                y /= weightSum;

                setTarget(new Vector2f(x, y));
            } else if (resultCount == 1f) {
                //System.out.println("1 result");
                for (Vector2f v : results) {
                    if (v == null)
                        continue;
                    x = v.x;
                    y = v.y;
                }

                setTarget(new Vector2f(x, y));
            }
        }

        Vector2f[] results = new Vector2f[fireDNA.size()];
        int resultCount = 0;
        for (int i = 0; i < results.length; i++) {
            results[i] = fireDNA.get(i).execute(this);

            if (results[i] != null)
                resultCount++;
        }

        float x = 0f;
        float y = 0f;
        if (resultCount > 1) {
            //System.out.println(resultCount + " results");
            float weightSum = 0f;
            for (int i = 0; i < results.length; i++) {
                if (results[i] != null) {
                    weightSum += fireDNA.get(i).getWeignt();
                    x += (fireDNA.get(i).getWeignt() * results[i].getX());
                    y += (fireDNA.get(i).getWeignt() * results[i].getY());
                }
            }

            x /= weightSum;
            y /= weightSum;

            useAbility(x, y, 0);
        } else if (resultCount == 1f) {
            //System.out.println("1 result");
            for (Vector2f v : results) {
                if (v == null)
                    continue;
                x = v.x;
                y = v.y;
            }

            useAbility(x, y, 0);
        }

        super.tick();
    }
}
