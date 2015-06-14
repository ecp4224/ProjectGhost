package me.eddiep.ghost.server.game.queue.impl;

import me.eddiep.ghost.server.game.entities.abilities.Laser;
import me.eddiep.ghost.server.game.entities.playable.Playable;
import me.eddiep.ghost.server.game.queue.Queues;
import me.eddiep.ghost.server.game.util.VisibleFunction;

public class LaserQueue extends DemoQueue {
    @Override
    public void setupPlayer(Playable p) {
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
