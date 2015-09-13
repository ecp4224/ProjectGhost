package me.eddiep.ghost.gameserver.api.network.impl;

import me.eddiep.ghost.common.game.MatchCreator;
import me.eddiep.ghost.common.game.NetworkMatch;
import me.eddiep.ghost.common.network.BaseServer;
import me.eddiep.ghost.common.network.world.NetworkWorld;
import me.eddiep.ghost.game.match.Match;
import me.eddiep.ghost.game.queue.Queues;
import me.eddiep.ghost.game.stats.MatchHistory;
import me.eddiep.ghost.game.team.Team;
import me.eddiep.ghost.gameserver.api.GameServer;
import me.eddiep.ghost.gameserver.api.network.packets.MatchHistoryPacket;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class BasicMatchFactory implements MatchCreator {
    private HashMap<Long, NetworkMatch> activeMatches = new HashMap<>();

    @Override
    public NetworkMatch createMatchFor(Team team1, Team team2, long id, Queues queue, String map, BaseServer server) throws IOException {
        NetworkMatch match = new NetworkMatch(team1, team2, server);
        NetworkWorld world = new NetworkWorld(map, match);
        match.setQueueType(queue);
        match.setWorld(world);
        GameServer.getGame().onMatchPreSetup(match);
        match.setup();
        match.setID(id);

        activeMatches.put(match.getID(), match);

        return match;
    }

    @Override
    public void endAndSaveMatch(NetworkMatch match) {
        if (match.disconnectdPlayers.size() > 0) { //Players disconnected during this match!
            System.out.println(match.disconnectdPlayers.size() + " players disconnected from this match!");
        }
        activeMatches.remove(match.getID());

        saveMatchInfo(match.matchHistory());

        match.dispose();
    }

    @Override
    public void saveMatchInfo(MatchHistory match) {
        MatchHistoryPacket packet = new MatchHistoryPacket(GameServer.getMatchmakingClient());
        try {
            packet.writePacket(match);
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Failed to save match!");
        }
    }

    @Override
    public Match findMatch(long id) {
        if (activeMatches.containsKey(id))
            return activeMatches.get(id);
        else {
            return null;
        }
    }

    @Override
    public List<NetworkMatch> getAllActiveMatches() {
        ArrayList<NetworkMatch> matches = new ArrayList<>();

        for (Long id : activeMatches.keySet()) {
            matches.add(activeMatches.get(id));
        }

        return Collections.unmodifiableList(matches);
    }

    @Override
    public void createMatchFor(NetworkMatch match, long id, Queues queue, String mapName, BaseServer server) {
        throw new IllegalAccessError("Not implemented!");
    }
}
