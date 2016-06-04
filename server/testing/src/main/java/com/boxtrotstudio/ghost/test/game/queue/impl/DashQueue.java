package com.boxtrotstudio.ghost.test.game.queue.impl;

import com.boxtrotstudio.ghost.game.match.abilities.Dash;
import com.boxtrotstudio.ghost.game.match.entities.PlayableEntity;
import com.boxtrotstudio.ghost.game.queue.Queues;
import com.boxtrotstudio.ghost.game.util.VisibleFunction;

public class DashQueue extends DemoQueue {
    @Override
    public Queues queue() {
        return Queues.DASH;
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
        p.setCurrentAbility(Dash.class);
        p.setVisibleFunction(VisibleFunction.ORGINAL);
        p.setLives((byte) 3);
    }
}
