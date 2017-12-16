package com.boxtrotstudio.ghost.test.game.queue.impl;

import com.boxtrotstudio.ghost.common.game.NetworkMatch;
import com.boxtrotstudio.ghost.game.match.entities.PlayableEntity;
import com.boxtrotstudio.ghost.game.queue.Queues;
import com.boxtrotstudio.ghost.game.util.VisibleFunction;

import java.io.IOException;

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
    public void setupPlayer(PlayableEntity p) {
        p.setLives((byte) 3);
        p.setVisibleFunction(VisibleFunction.ORGINAL);
    }

    @Override
    public NetworkMatch createMatch(String user1, String user2) throws IOException {
        NetworkMatch match = super.createMatch(user1, user2);

        match.setTimed(true, 120);

        return match;
    }
}
