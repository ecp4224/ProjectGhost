package me.eddiep.ghost.ai;

import com.boxtrotstudio.ghost.game.match.Match;
import com.boxtrotstudio.ghost.game.match.entities.PlayableEntity;
import com.boxtrotstudio.ghost.game.match.entities.playable.BasePlayableEntity;
import com.boxtrotstudio.ghost.game.match.stats.TemporaryStats;
import com.boxtrotstudio.ghost.utils.Vector2f;
import org.encog.neural.networks.BasicNetwork;

import java.util.HashMap;

public class SmartAI extends BasePlayableEntity {
    private HashMap<Short, Vector2f> lastSeen = new HashMap<>();
    private BasicNetwork network;

    public SmartAI(BasicNetwork network) {
        this.network = network;
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

    @Override
    public void onDamage(PlayableEntity damager) {
        super.onDamage(damager);


    }

    private long lastMove;
    @Override
    public void tick() {
        //Update last seen
        for (PlayableEntity p : getOpponents()) {
            if (p.isVisible()) {
                lastSeen.put(p.getID(), p.getPosition().cloneVector());
            }
        }

        /*
        0 = currentX
        1 = currentY
        2 = velocityX
        3 = velocityY
        4 = targetX
        5 = targetY
        6 = isVisible
        7 = lives
         */

        //if (!hasTarget()) {
        double myX = getX();
        double myY = getY();
        PlayableEntity p = getOpponents()[0];
        float theirX, theirY;

        theirX = p.getX();
        theirY = p.getY();
            /*if (p.isVisible()) {
                theirX = p.getX();
                theirY = p.getY();
            } else if (lastSeen.containsKey(p.getID())) {
                theirX = lastSeen.get(p.getID()).x;
                theirY = lastSeen.get(p.getID()).y;
            } else {
                super.tick();
                setVisible(true);
                lastMove = System.currentTimeMillis();
                return;
            }*/

        double myLives = getLives();
        double theirLives = getOpponents()[0].getLives();

        Vector2f vel = new Vector2f(p.getXVelocity(), p.getYVelocity());

        double[] input = new double[]{
                myX,
                myY,
                theirX,
                theirY,
                myLives,
                theirLives,
                0.0,
                p.isVisible() ? 1.0 : 0.0,
                vel.x,
                vel.y
        };

        double[] output = new double[2];
        //double[] output2 = new double[1];

        network.compute(input, output);

        //setVelocity(new Vector2f((float)output[0], (float)output[1]));

        output[0] = Math.max(Math.min(1.0, output[0]), 0.0);
        output[1] = Math.max(Math.min(1.0, output[1]), 0.0);

        Vector2f target = new Vector2f((float) output[0], (float) output[1]);
        target.x *= 1024f;
        target.y *= 720f;

        setTarget(target);

        System.out.println(output[0] + ", " + output[1] + " : " + getTarget().x + ", " + getTarget().y);

        lastMove = System.currentTimeMillis();
        //}


        super.tick();
        //}

        setVisible(true);
    }
}
