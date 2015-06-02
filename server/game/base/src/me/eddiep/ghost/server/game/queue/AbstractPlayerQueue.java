package me.eddiep.ghost.server.game.queue;

import me.eddiep.ghost.server.game.Game;
import me.eddiep.ghost.server.game.MatchFactory;
import me.eddiep.ghost.server.game.team.Team;
import me.eddiep.ghost.server.game.entities.playable.impl.Player;
import me.eddiep.ghost.server.game.entities.playable.impl.PlayerFactory;

import java.io.IOException;
import java.util.*;

public abstract class AbstractPlayerQueue implements PlayerQueue {
    private List<UUID> playerQueue = new ArrayList<>();
    private final Game owner;

    public AbstractPlayerQueue(Game owner) {
        this.owner = owner;
    }

    @Override
    public void addUserToQueue(Player player) {
        if (player.isInQueue())
            return;

        playerQueue.add(player.getSession());
        player.setQueue(this);

        System.out.println("[SERVER] " + player.getUsername() + " has joined the " + owner.name() + " queue!");
    }

    @Override
    public void removeUserFromQueue(Player player) {
        if (!player.isInQueue())
            return;

        playerQueue.remove(player.getSession());
        player.setQueue(null);
        System.out.println("[SERVER] " + player.getUsername() + " has left the " + owner.name() + " queue!");
    }

    @Override
    public int playerCount() {
        return playerQueue.size();
    }

    @Override
    public void processQueue() {
        int max = playerQueue.size();
        if (playerQueue.size() >= 100) {
            max = max / 4;
        }

        List<UUID> process = playerQueue.subList(0, max);

        playerQueue.removeAll(onProcessQueue(process));
    }

    @Override
    public Game owner() {
        return owner;
    }

    protected abstract List<UUID> onProcessQueue(List<UUID> queueToProcess);

    public void createMatch(UUID user1, UUID user2) throws IOException {
        Player player1 = PlayerFactory.findPlayerByUUID(user1);
        Player player2 = PlayerFactory.findPlayerByUUID(user2);
        QueueDescription queueDescription = new QueueDescription(owner);

        MatchFactory.createMatchFor(player1, player2, queueDescription);

        player1.setQueue(null);
        player2.setQueue(null);
    }

    public void createMatch(Team team1, Team team2) throws IOException {
        QueueDescription queueDescription = new QueueDescription(owner);

        MatchFactory.createMatchFor(team1, team2, queueDescription);

        for (Player p : team1.getTeamMembers()) {
            p.setQueue(null);
        }

        for (Player p : team2.getTeamMembers()) {
            p.setQueue(null);
        }
    }
}
