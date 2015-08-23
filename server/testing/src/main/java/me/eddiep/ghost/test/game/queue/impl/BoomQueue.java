package me.eddiep.ghost.test.game.queue.impl;

import me.eddiep.ghost.game.match.abilities.Boomerang;
import me.eddiep.ghost.game.match.entities.PlayableEntity;
import me.eddiep.ghost.game.queue.Queues;
import me.eddiep.ghost.game.util.VisibleFunction;

public class BoomQueue extends DemoQueue{
    @Override
    public Queues queue() {
        return Queues.BOOM;
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
    public void setupPlayer(PlayableEntity p) {
        p.setCurrentAbility(Boomerang.class);
        p.setVisibleFunction(VisibleFunction.ORGINAL);
        p.setLives((byte) 3);
    }
}
