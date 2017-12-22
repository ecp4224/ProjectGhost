package com.boxtrotstudio.ghost.test.game.queue.impl;

import com.boxtrotstudio.ghost.game.match.abilities.Boomerang;
import com.boxtrotstudio.ghost.game.match.entities.PlayableEntity;
import com.boxtrotstudio.ghost.game.queue.Queues;
import com.boxtrotstudio.ghost.game.util.VisibleFunction;

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
        p.setCurrentAbility(new Boomerang(p));
        p.setVisibleFunction(VisibleFunction.ORIGINAL);
        p.setLives((byte) 3);
    }
}
