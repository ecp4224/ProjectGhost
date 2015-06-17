package me.eddiep.ghost.server.game.queue.impl;

import me.eddiep.ghost.server.game.entities.abilities.Circle;
import me.eddiep.ghost.server.game.entities.abilities.Gun;
import me.eddiep.ghost.server.game.entities.playable.Playable;
import me.eddiep.ghost.server.game.queue.Queues;
import me.eddiep.ghost.server.game.util.VisibleFunction;

public class OriginalQueue extends DemoQueue {
    @Override
    public Queues queue() {
        return Queues.ORIGINAL;
    }

    @Override
    public int allyCount() {
        return 0;
    }

    @Override
    public int opponentCount() {
        return 1;
    }

    @Override
    public String description() {
        return "Face a random opponent in a 1v1 match to the death. [3 Lives]";
    }

    @Override
    public void setupPlayer(Playable p) {
        p.setCurrentAbility(Gun.class);
        p.setVisibleFunction(VisibleFunction.ORGINAL);
        p.setLives((byte) 3);
    }
}
