package me.eddiep.ghost.gameserver.api.network.impl;

import me.eddiep.ghost.game.match.Match;
import me.eddiep.ghost.game.stats.MatchHistory;
import me.eddiep.ghost.game.team.Team;
import me.eddiep.ghost.gameserver.api.GameServer;
import me.eddiep.ghost.gameserver.api.network.MatchFactory;
import me.eddiep.ghost.gameserver.api.network.NetworkMatch;
import me.eddiep.ghost.gameserver.api.network.TcpUdpServer;
import me.eddiep.ghost.gameserver.api.network.packets.MatchHistoryPacket;
import me.eddiep.ghost.gameserver.api.network.world.NetworkWorld;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class BasicMatchFactory implements MatchFactory {
    private HashMap<Long, NetworkMatch> activeMatches = new HashMap<>();

    @Override
    public NetworkMatch createMatchFor(Team team1, Team team2, long id, TcpUdpServer server) throws IOException {
        NetworkMatch match = new NetworkMatch(team1, team2, server);
        NetworkWorld world = new NetworkWorld(match);
        match.setQueueType(GameServer.getGame().getQueue());
        match.setWorld(world);
        GameServer.getGame().onMatchPreSetup(match);
        match.setup();
        match.setID(id);

        activeMatches.put(match.getID(), match);

        return match;
    }

    @Override
    public void endAndSaveMatch(NetworkMatch match) {
        System.out.println("[SERVER] Saving and Disposing Match: " + match.getID());
        activeMatches.remove(match.getID());

        saveMatchInfo(match.matchHistory());

        match.dispose();
    }

    @Override
    public void saveMatchInfo(MatchHistory match) {
        //TODO Send match history to matchmaking server somehow...
        //TODO This data is pretty big, so maybe send in chunks ?
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
}
