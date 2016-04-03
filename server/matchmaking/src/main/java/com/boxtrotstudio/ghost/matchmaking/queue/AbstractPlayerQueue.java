package com.boxtrotstudio.ghost.matchmaking.queue;

import com.boxtrotstudio.ghost.matchmaking.network.gameserver.GameServer;
import com.boxtrotstudio.ghost.matchmaking.network.gameserver.GameServerFactory;
import com.boxtrotstudio.ghost.matchmaking.network.gameserver.Stream;
import com.boxtrotstudio.ghost.matchmaking.player.Player;
import com.boxtrotstudio.ghost.utils.ArrayHelper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractPlayerQueue implements PlayerQueue {
    private List<Player> playerQueue = new ArrayList<>();
    private final Stream stream;
    public long queueProcessStart;

    public AbstractPlayerQueue(Stream stream) {
        this.stream = stream;
    }

    @Override
    public final Stream getStream() {
        return stream;
    }

    @Override
    public void addUserToQueue(Player player) {
        if (player.isInQueue() || player.getStream() != stream)
            return;

        playerQueue.add(player);
        player.setQueue(this);

        System.out.println("[SERVER] " + player.getUsername() + " has joined the " + queue().name() + " queue!");
    }

    @Override
    public void removeUserFromQueue(Player player) {
        if (!player.isInQueue() || player.getStream() != stream)
            return;

        playerQueue.remove(player);
        player.setQueue(null);
        System.out.println("[SERVER] " + player.getUsername() + " has left the " + queue().name() + " queue!");
    }

    private int expand = 1;
    @Override
    public void processQueue() {
        int max = playerQueue.size();
        if (playerQueue.size() >= 100 && expand < 4) {
            max = max / 4;

            max *= expand;
        }

        List<Player> process = new ArrayList<>(playerQueue.subList(0, max));

        queueProcessStart = System.currentTimeMillis();
        List<Player> matches = onProcessQueue(process);

        playerQueue.removeAll(matches);

        if (matches.size() < process.size() / 2) {
            expand++;
        } else {
            expand = 1;
        }
    }

    @Override
    public long getProcessStartTime() {
        return queueProcessStart;
    }

    protected abstract List<Player> onProcessQueue(List<Player> queueToProcess);

    public boolean createMatch(Player player1, Player player2) throws IOException {
        GameServer server = GameServerFactory.createMatchFor(queue(), new Player[] { player1 }, new Player[] { player2 }, getStream());
        if (server == null)
            return false;

        player1.setQueue(null);
        player1.setInMatch(true);

        player2.setQueue(null);
        player2.setInMatch(true);

        return true;
    }

    public boolean createMatch(Player[] team1, Player[] team2) throws IOException {
        GameServer server = GameServerFactory.createMatchFor(queue(), team1, team2, getStream());
        if (server == null)
            return false;

        for (Player p : ArrayHelper.combine(team1, team2)) {
            p.setQueue(null);
            p.setInMatch(true);
        }

        return true;
    }
}
