package com.boxtrotstudio.ghost.matchmaking.queue.impl;

import com.boxtrotstudio.ghost.game.match.abilities.Gun;
import com.boxtrotstudio.ghost.game.match.entities.PlayableEntity;
import com.boxtrotstudio.ghost.game.queue.Queues;
import com.boxtrotstudio.ghost.game.team.Team;
import com.boxtrotstudio.ghost.matchmaking.network.gameserver.Stream;
import com.boxtrotstudio.ghost.matchmaking.player.Player;
import com.boxtrotstudio.ghost.matchmaking.queue.AbstractPlayerQueue;
import com.boxtrotstudio.ghost.utils.ArrayHelper;
import com.boxtrotstudio.ghost.utils.Global;
import com.boxtrotstudio.ghost.utils.PRunnable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TutorialQueue extends AbstractPlayerQueue {

    public TutorialQueue(Stream stream) {
        super(stream);
    }

    @Override
    protected List<Player> onProcessQueue(List<Player> toProcess) {
        List<Player> toRemove = new ArrayList<>();

        while (toProcess.size() > 0) {
            Player p = toProcess.get(0);
            toProcess.remove(0);

            try {
                createMatch(new Player[] { p }, new Player[0]);
            } catch (IOException e) {
                e.printStackTrace();
            }

            toRemove.add(p);
        }

        return toRemove;
    }

    @Override
    public String description() {
        return "A simple queue allowing 1 opponent, used for Tutorial Match";
    }

    @Override
    public Queues queue() {
        return Queues.TUTORIAL;
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
