package me.eddiep.ghost.ai;

import com.boxtrotstudio.ghost.game.match.Match;
import com.boxtrotstudio.ghost.game.match.entities.PlayableEntity;
import com.boxtrotstudio.ghost.game.match.entities.playable.BasePlayableEntity;
import com.boxtrotstudio.ghost.game.match.stats.TemporaryStats;
import com.boxtrotstudio.ghost.utils.Global;
import com.boxtrotstudio.ghost.utils.Vector2f;
import me.eddiep.ghost.ai.dna.Sequence;
import me.eddiep.ghost.ai.dna.fire.LastSeenFiring;
import me.eddiep.ghost.ai.dna.fire.PredictFiring;
import me.eddiep.ghost.ai.dna.fire.RandomFiring;
import me.eddiep.ghost.ai.dna.movement.AvoidMovement;
import me.eddiep.ghost.ai.dna.movement.RandomMovement;
import me.eddiep.ghost.ai.dna.movement.SeekMovement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SmartAI extends BasePlayableEntity {
    private List<Sequence<Vector2f>> movementDNA = new ArrayList<>();
    private List<Sequence<Vector2f>> fireDNA = new ArrayList<>();
    private long fireTimeout;

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
            switch (Global.RANDOM.nextInt(3)) {
                case 0:
                    fireDNA.add(new LastSeenFiring());
                    break;
                case 1:
                    fireDNA.add(new RandomFiring());
                    break;
                case 2:
                    fireDNA.add(new PredictFiring());
                    break;
            }
        }

        fireTimeout = Global.RANDOM.nextInt(3000) + 1000;

        setName("SMART_BOT");
        setReady(true);
    }

    public SmartAI(List<Sequence<Vector2f>> movementDNA, List<Sequence<Vector2f>> fire, long fireTimeout) {
        this.movementDNA = movementDNA;
        this.fireDNA = fire;
        this.fireTimeout = fireTimeout;

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
            } else if (Global.RANDOM.nextDouble() < 0.01) {
                mate.mutate();
            }

            movementNewDNA.add(mate);
        }

        ArrayList<Sequence<Vector2f>> fireDNA = new ArrayList<>();

        for (int i = 0; i < this.fireDNA.size(); i++) {
            Sequence<Vector2f> ours = this.fireDNA.get(i);
            Sequence<Vector2f> theirs = ai.fireDNA.get(i);

            Sequence<Vector2f> mate = ours.combine(theirs);
            if (mate == null) {
                mate = Global.RANDOM.nextBoolean() ? ours : theirs;
                mate.mutate();
            } else if (Global.RANDOM.nextDouble() < 0.01) {
                mate.mutate();
            }

            fireDNA.add(mate);
        }

        long fireTimeout = (long) ((this.fireTimeout + ai.fireTimeout)  / 2.0);
        if (Global.RANDOM.nextDouble() < 0.01)
            fireTimeout = Global.RANDOM.nextInt(3000) + 1000;

        System.out.println("Made new SmartAI");
        System.out.println("Movement: ");
        for (Sequence s : movementNewDNA) {
            System.out.println("    " + s);
        }
        System.out.println("Fire (" + fireTimeout + "ms timeout): ");
        for (Sequence s : fireDNA) {
            System.out.println("    " + s);
        }

        return new SmartAI(movementNewDNA, fireDNA, fireTimeout);
    }

    @Override
    public void onDamage(PlayableEntity damager) {
        super.onDamage(damager);
    }

    private long lastFire;
    @Override
    public void tick() {
        if (!isFrozen() && !isDead() && !containingMatch.hasMatchEnded()) {

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

            if (System.currentTimeMillis() - lastFire >= fireTimeout || Global.RANDOM.nextDouble() < 0.1) {
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
                    lastFire = System.currentTimeMillis();
                } else if (resultCount == 1f) {
                    //System.out.println("1 result");
                    for (Vector2f v : results) {
                        if (v == null)
                            continue;
                        x = v.x;
                        y = v.y;
                    }

                    useAbility(x, y, 0);
                    lastFire = System.currentTimeMillis();
                }
            }
        }

        super.tick();
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Movement: \n");
        for (Sequence s : movementDNA) {
            builder.append("    ").append(s).append("\n");
        }
        builder.append("Fire (").append(fireTimeout).append("ms timeout) \n");
        for (Sequence s : fireDNA) {
            builder.append("    ").append(s).append("\n");
        }

        return builder.toString();
    }
}
