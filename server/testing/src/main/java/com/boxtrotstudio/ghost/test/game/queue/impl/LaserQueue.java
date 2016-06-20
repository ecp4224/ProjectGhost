package com.boxtrotstudio.ghost.test.game.queue.impl;

import com.boxtrotstudio.ghost.game.match.abilities.Laser;
import com.boxtrotstudio.ghost.game.match.entities.PlayableEntity;
import com.boxtrotstudio.ghost.game.queue.Queues;
import com.boxtrotstudio.ghost.game.util.VisibleFunction;

public class LaserQueue extends DemoQueue {
    @Override
    public void setupPlayer(PlayableEntity p) {
        p.setLives((byte) 3);
        p.setVisibleFunction(VisibleFunction.ORGINAL);
        p._packet_setCurrentAbility(Laser.class);
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
