package me.eddiep.ghost.gameserver.api.network.impl;

import me.eddiep.ghost.game.match.Match;
import me.eddiep.ghost.game.stats.MatchHistory;
import me.eddiep.ghost.game.team.Team;
import me.eddiep.ghost.gameserver.api.GameServer;
import me.eddiep.ghost.gameserver.api.network.ActiveMatch;
import me.eddiep.ghost.gameserver.api.network.MatchFactory;
import me.eddiep.ghost.gameserver.api.network.TcpUdpServer;
import me.eddiep.ghost.gameserver.api.network.packets.MatchHistoryPacket;
import me.eddiep.ghost.utils.Global;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class BasicMatchFactory implements MatchFactory {
    private HashMap<Long, ActiveMatch> activeMatches = new HashMap<>();

    @Override
    public ActiveMatch createMatchFor(Team team1, Team team2, TcpUdpServer server) throws IOException {
        ActiveMatch match = new ActiveMatch(team1, team2, server);
        match.setQueueType(GameServer.getGame().getQueue());
        match.setup();
        long id = Global.SQL.getStoredMatchCount() + activeMatches.size();
        match.setID(id);

        activeMatches.put(match.getID(), match);

        return match;
    }

    @Override
    public void endAndSaveMatch(ActiveMatch match) {
        System.out.println("[SERVER] Saving and Disposing Match: " + match.getID());
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
            return Global.SQL.fetchMatch(id);
        }
    }

    @Override
    public List<ActiveMatch> getAllActiveMatches() {
        ArrayList<ActiveMatch> matches = new ArrayList<>();

        for (Long id : activeMatches.keySet()) {
            matches.add(activeMatches.get(id));
        }

        return Collections.unmodifiableList(matches);
    }
}
