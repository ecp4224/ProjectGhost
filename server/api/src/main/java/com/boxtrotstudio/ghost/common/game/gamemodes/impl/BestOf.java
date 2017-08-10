package com.boxtrotstudio.ghost.common.game.gamemodes.impl;

import com.boxtrotstudio.ghost.common.game.NetworkMatch;
import com.boxtrotstudio.ghost.common.game.Player;
import com.boxtrotstudio.ghost.common.game.User;
import com.boxtrotstudio.ghost.game.match.Event;
import com.boxtrotstudio.ghost.game.match.entities.PlayableEntity;
import com.boxtrotstudio.ghost.game.team.Team;
import com.boxtrotstudio.ghost.network.Server;
import com.boxtrotstudio.ghost.utils.Condition;
import com.boxtrotstudio.ghost.utils.PRunnable;

public class BestOf extends NetworkMatch {
    private int winsRequired = 2;

    private int team1WinCount = 0, team2WinCount = 0;
    private int roundCount = 0;
    public BestOf(Team team1, Team team2, Server server) {
        super(team1, team2, server);
    }

    public BestOf(Team team1, Team team2, Server server, int winsRequired) {
        super(team1, team2, server);
        this.winsRequired = winsRequired;
    }

    @Override
    protected void stage() {
        do {
            waitFor(() -> team1.isTeamDead() || team2.isTeamDead());

            if (entireTeamDisconnected(team1) || entireTeamDisconnected(team2)) {
                //The logic for team disconnecting is handled by NetworkMatch
                //We just need to make sure we don't do anything when a team
                //disconnects
                break; //Don't continue the game if a team has disconnected
            }

            roundCount++;
            try {
                Thread.sleep(700);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            Team winner, loser;
            if (team1.isTeamAlive()) {
                winner = team1;
                loser = team2;
            }
            else if (team2.isTeamAlive()) {
                winner = team2;
                loser = team1;
            }
            else {
                loser = null;
                winner = null;
            }

            if (winner != null) {
                winner.triggerEvent(Event.TeamWin, roundCount);
                loser.triggerEvent(Event.TeamLose, roundCount);

                /*
                We have to wait a few moments after triggering the events
                to ensure the clients receives them. Once they do we can
                halt the timeline using setActive(false)
                 */
                try {
                    Thread.sleep(600);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                setActive(false, "Round Over");

                executeOnAllConnected(p -> {
                    Player player = p.getClient().getPlayer();

                    if (winner.isAlly(player)) {
                        player.sendMatchMessage("Round Won!");
                    } else {
                        player.sendMatchMessage("Round Lost..");
                    }
                });

                softWin(winner, loser);
            } else {
                setMatchMessage("Draw!");
                //TODO Handle draw
            }
        } while (team1WinCount < winsRequired && team2WinCount < winsRequired);
    }

    private void softWin(Team winner, Team loser) {
        if (winner.getTeamNumber() == 1) {
            team1WinCount++;

            if (team1WinCount >= winsRequired) {
                end(winner);
                return;
            }
        } else {
            team2WinCount++;
            if (team2WinCount >= winsRequired) {
                end(winner);
                return;
            }
        }

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        beginIntermission();
    }

    private void beginIntermission() {
        team1.unready();
        team2.unready();

        startCountdown(15, "Next round will start in %t seconds..", this::endIntermission);

        when(() -> team1.isTeamReady() && team2.isTeamReady()).execute(() -> {
            cancelCountdown();
            endIntermission();
        });

        team1.resetLives();
        team2.resetLives();
    }

    private void endIntermission() {
        respawnTeams();

        setActive(true, "");
    }
}
