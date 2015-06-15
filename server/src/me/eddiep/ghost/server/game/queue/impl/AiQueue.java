package me.eddiep.ghost.server.game.queue.impl;

import me.eddiep.ghost.server.Main;
import me.eddiep.ghost.server.game.entities.playable.Playable;
import me.eddiep.ghost.server.game.entities.playable.impl.BasicAI;
import me.eddiep.ghost.server.game.queue.AbstractPlayerQueue;
import me.eddiep.ghost.server.game.queue.Queues;
import me.eddiep.ghost.server.game.team.Team;
import me.eddiep.ghost.server.game.util.VisibleFunction;
import me.eddiep.ghost.server.utils.ArrayHelper;
import me.eddiep.ghost.server.utils.PRunnable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AiQueue extends AbstractPlayerQueue {
    @Override
    protected List<UUID> onProcessQueue(List<UUID> queueToProcess) {
        List<UUID> toRemove = new ArrayList<>();


        while (queueToProcess.size() > 0) {
            int randomIndex = Main.RANDOM.nextInt(queueToProcess.size());

            UUID id1 = queueToProcess.get(randomIndex);
            Team team1 = new Team(1, id1);
            Team team2 = new Team(2, new BasicAI(), new BasicAI());

            try {
                createMatch(team1, team2);
                queueToProcess.remove(randomIndex);

                toRemove.add(id1);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return toRemove;
    }

    @Override
    public String description() {
        return "Face a basic AI!";
    }

    @Override
    public Queues queue() {
        return Queues.AI;
    }

    @Override
    public int allyCount() {
        return 0;
    }

    @Override
    public int opponentCount() {
        return 2;
    }

    @Override
    protected void onTeamEnterMatch(Team team1, Team team2) {
        super.onTeamEnterMatch(team1, team2);

        ArrayHelper.forEach(
                ArrayHelper.combind(team1.getTeamMembers(), team2.getTeamMembers()),
                new PRunnable<Playable>() {
                    @Override
                    public void run(Playable p) {
                        p.setLives((byte) 3);
                        p.setVisibleFunction(VisibleFunction.ORGINAL);
                    }
                }
        );
    }
}
