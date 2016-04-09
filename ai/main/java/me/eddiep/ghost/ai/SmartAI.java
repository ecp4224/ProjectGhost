package me.eddiep.ghost.ai;

import com.boxtrotstudio.ghost.game.match.Match;
import com.boxtrotstudio.ghost.game.match.entities.PlayableEntity;
import com.boxtrotstudio.ghost.game.match.entities.playable.BasePlayableEntity;
import com.boxtrotstudio.ghost.game.match.stats.TemporaryStats;
import com.boxtrotstudio.ghost.utils.Global;
import com.boxtrotstudio.ghost.utils.Vector2f;
import me.eddiep.ghost.ai.dna.Sequence;
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

    public SmartAI() {
        movementDNA.add(new RandomMovement());
        movementDNA.add(new AvoidMovement());
        movementDNA.add(new SeekMovement());

        setName("SMART_BOT");
        setReady(true);
    }

    public SmartAI(List<Sequence<Vector2f>> movementDNA) {
        this.movementDNA = movementDNA;

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
        ArrayList<Sequence<Vector2f>> newDNA = new ArrayList<>();

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

            newDNA.add(mate);
        }

        return new SmartAI(newDNA);
    }

    @Override
    public void onDamage(PlayableEntity damager) {
        super.onDamage(damager);
    }

    @Override
    public void tick() {
        Vector2f[] results = new Vector2f[movementDNA.size()];
        for (int i = 0; i < results.length; i++) {
            results[i] = movementDNA.get(i).execute(this);
        }

        float weightSum = 0f;
        float x = 0f;
        float y = 0f;
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

        setVisible(true);
    }
}
