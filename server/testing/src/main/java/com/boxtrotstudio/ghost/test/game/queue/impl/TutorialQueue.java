package com.boxtrotstudio.ghost.test.game.queue.impl;

import com.boxtrotstudio.ghost.common.game.PlayerFactory;
import com.boxtrotstudio.ghost.common.game.gamemodes.tutorial.TutorialBot;
import com.boxtrotstudio.ghost.common.game.gamemodes.tutorial.TutorialMatch;
import com.boxtrotstudio.ghost.game.match.abilities.Gun;
import com.boxtrotstudio.ghost.game.match.entities.PlayableEntity;
import com.boxtrotstudio.ghost.game.queue.Queues;
import com.boxtrotstudio.ghost.game.team.Team;
import com.boxtrotstudio.ghost.test.Main;
import com.boxtrotstudio.ghost.test.game.queue.AbstractPlayerQueue;
import com.boxtrotstudio.ghost.utils.ArrayHelper;
import com.boxtrotstudio.ghost.utils.Global;

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

            PlayableEntity player = PlayerFactory.getCreator().findPlayerByUUID(id1);
            Team playerTeam = new Team(1, player);
            Team botTeam = new Team(2, new TutorialBot(player));

            TutorialMatch tutorialMatch = new TutorialMatch(playerTeam, botTeam, Main.TCP_UDP_SERVER);
            try {
                createMatch(tutorialMatch, "Scene1");
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
                p -> {
                    p.setLives((byte) 3);
                    p.setCurrentAbility(new Gun(p));
                }
        );
    }
}
