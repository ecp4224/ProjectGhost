package me.eddiep.ghost.test.game.queue.impl;

import me.eddiep.ghost.game.match.abilities.Laser;
import me.eddiep.ghost.game.match.entities.PlayableEntity;
import me.eddiep.ghost.game.queue.Queues;
import me.eddiep.ghost.game.util.VisibleFunction;

public class LaserQueue extends DemoQueue {
    @Override
    public void setupPlayer(PlayableEntity p) {
        p.setLives((byte) 3);
        p.setVisibleFunction(VisibleFunction.ORGINAL);
        p.setCurrentAbility(Laser.class);
    }

    @Override
    public String description() {
        return "Fire dem lasers";
    }

    @Override
    public Queues queue() {
        return Queues.LASER;
    }

    @Override
    public int allyCount() {
        return 0;
    }

    @Override
    public int opponentCount() {
        return 1;
    }
}
