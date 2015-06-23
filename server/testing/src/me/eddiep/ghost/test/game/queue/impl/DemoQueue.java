package me.eddiep.ghost.test.game.queue.impl;

import me.eddiep.ghost.game.entities.PlayableEntity;
import me.eddiep.ghost.game.team.Team;
import me.eddiep.ghost.test.game.queue.AbstractPlayerQueue;
import me.eddiep.ghost.utils.ArrayHelper;
import me.eddiep.ghost.utils.Global;
import me.eddiep.ghost.utils.PRunnable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public abstract class DemoQueue extends AbstractPlayerQueue {
    @Override
    protected List<UUID> onProcessQueue(List<UUID> toProcess) {
        List<UUID> toRemove = new ArrayList<>();

        List<UUID> queueToProcess = new ArrayList<>(toProcess);

        while (queueToProcess.size() > 1) {
            int randomIndex = Global.RANDOM.nextInt(queueToProcess.size());
            int randomIndex2;
            do {
                randomIndex2 = Global.RANDOM.nextInt(queueToProcess.size());
            } while (randomIndex2 == randomIndex);

            UUID id1 = queueToProcess.get(randomIndex);
            UUID id2 = queueToProcess.get(randomIndex2);

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
                ArrayHelper.combind(team1.getTeamMembers(), team2.getTeamMembers()),
                new PRunnable<PlayableEntity>() {
                    @Override
                    public void run(PlayableEntity p) {
                        setupPlayer(p);
                    }
                }
        );
    }

    public abstract void setupPlayer(PlayableEntity p);
}
