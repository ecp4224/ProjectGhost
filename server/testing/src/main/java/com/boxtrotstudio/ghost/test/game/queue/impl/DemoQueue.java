package com.boxtrotstudio.ghost.test.game.queue.impl;

import com.boxtrotstudio.ghost.game.match.entities.PlayableEntity;
import com.boxtrotstudio.ghost.game.team.Team;
import com.boxtrotstudio.ghost.test.game.queue.AbstractPlayerQueue;
import com.boxtrotstudio.ghost.utils.ArrayHelper;
import com.boxtrotstudio.ghost.utils.Global;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public abstract class DemoQueue extends AbstractPlayerQueue {
    @Override
    protected List<String> onProcessQueue(List<String> toProcess) {
        List<String> toRemove = new ArrayList<>();

        List<String> queueToProcess = new ArrayList<>(toProcess);

        while (queueToProcess.size() > 1) {
            int randomIndex = Global.RANDOM.nextInt(queueToProcess.size());
            int randomIndex2;
            do {
                randomIndex2 = Global.RANDOM.nextInt(queueToProcess.size());
            } while (randomIndex2 == randomIndex);

            String id1 = queueToProcess.get(randomIndex);
            String id2 = queueToProcess.get(randomIndex2);

            try {
                createMatch(id1, id2);

                int toRemove2 = randomIndex2 > randomIndex ? randomIndex2 - 1 : randomIndex2;

                queueToProcess.remove(randomIndex);
                queueToProcess.remove(toRemove2);

                toRemove.add(id1);
                toRemove.add(id2);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return toRemove;
    }

    @Override
    protected void onTeamEnterMatch(Team team1, Team team2) {
        super.onTeamEnterMatch(team1, team2);

        ArrayHelper.forEach(
                ArrayHelper.combine(team1.getTeamMembers(), team2.getTeamMembers()),
                this::setupPlayer
        );
    }

    public abstract void setupPlayer(PlayableEntity p);
}
