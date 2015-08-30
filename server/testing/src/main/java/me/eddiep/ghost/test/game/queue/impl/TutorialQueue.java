package me.eddiep.ghost.test.game.queue.impl;

import me.eddiep.ghost.common.game.PlayerFactory;
import me.eddiep.ghost.common.game.TutorialBot;
import me.eddiep.ghost.common.game.TutorialMatch;
import me.eddiep.ghost.game.match.abilities.Gun;
import me.eddiep.ghost.game.match.entities.PlayableEntity;
import me.eddiep.ghost.game.queue.Queues;
import me.eddiep.ghost.game.team.Team;
import me.eddiep.ghost.test.Main;
import me.eddiep.ghost.test.game.queue.AbstractPlayerQueue;
import me.eddiep.ghost.utils.ArrayHelper;
import me.eddiep.ghost.utils.Global;
import me.eddiep.ghost.utils.PRunnable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TutorialQueue extends AbstractPlayerQueue {

    @Override
    protected List<String> onProcessQueue(List<String> toProcess) {
        List<String> toRemove = new ArrayList<>();

        List<String> queueToProcess = new ArrayList<>(toProcess);

        while (queueToProcess.size() > 0) {
            int randomIndex = Global.RANDOM.nextInt(queueToProcess.size());
            String id1 = queueToProcess.get(randomIndex);

            Team playerTeam = new Team(1, PlayerFactory.getCreator().findPlayerByUUID(id1));
            Team botTeam = new Team(2, new TutorialBot());

            TutorialMatch tutorialMatch = new TutorialMatch(playerTeam, botTeam, Main.TCP_UDP_SERVER);
            try {
                createMatch(tutorialMatch);
            } catch (IOException e) {
                e.printStackTrace();
            }

            queueToProcess.remove(randomIndex);

            toRemove.add(id1);
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

    @Override
    public void onTeamEnterMatch(Team team1, Team team2) {
        super.onTeamEnterMatch(team1, team2);

        ArrayHelper.forEach(
                ArrayHelper.combine(team1.getTeamMembers(), team2.getTeamMembers()),
                new PRunnable<PlayableEntity>() {
                    @Override
                    public void run(PlayableEntity p) {
                        p.setLives((byte) 3);
                        p.setCurrentAbility(Gun.class);
                    }
                }
        );
    }
}
