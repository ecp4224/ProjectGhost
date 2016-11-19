package com.boxtrotstudio.ghost.test.game.queue;

import com.boxtrotstudio.ghost.common.game.NetworkMatch;
import com.boxtrotstudio.ghost.common.game.PlayerFactory;
import com.boxtrotstudio.ghost.game.match.Match;
import com.boxtrotstudio.ghost.game.match.entities.PlayableEntity;
import com.boxtrotstudio.ghost.game.team.Team;
import com.boxtrotstudio.ghost.test.game.TestPlayer;
import com.boxtrotstudio.ghost.utils.ArrayHelper;
import com.boxtrotstudio.ghost.utils.Global;
import com.boxtrotstudio.ghost.utils.PFunction;
import com.boxtrotstudio.ghost.common.game.MatchFactory;
import com.boxtrotstudio.ghost.common.game.Player;
import com.boxtrotstudio.ghost.game.queue.Queues;
import com.boxtrotstudio.ghost.test.Main;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public abstract class AbstractPlayerQueue implements PlayerQueue {
    private static final String[] MAPS = new String[] {
        "Scene1",
        "Scene2"
    };

    private List<String> playerQueue = new ArrayList<>();
    private static final HashMap<Queues, ArrayList<Long>> matches = new HashMap<Queues, ArrayList<Long>>();

    static {
        for (Queues t : Queues.values()) {
            matches.put(t, new ArrayList<Long>());
        }
    }

    public static List<Long> getMatchesFor(Queues type) {
        return Collections.unmodifiableList(matches.get(type));
    }

    @Override
    public void addUserToQueue(TestPlayer player) {
        if (player.isInQueue())
            return;

        playerQueue.add(player.getSession());
        player.setQueue(this);

        System.out.println("[SERVER] " + player.getUsername() + " has joined the " + queue().name() + " queue!");
    }

    @Override
    public void removeUserFromQueue(TestPlayer player) {
        if (!player.isInQueue())
            return;

        playerQueue.remove(player.getSession());
        player.setQueue(null);
        System.out.println("[SERVER] " + player.getUsername() + " has left the " + queue().name() + " queue!");
    }

    @Override
    public void processQueue() {
        int max = playerQueue.size();
        if (playerQueue.size() >= 100) {
            max = max / 4;
        }

        List<String> process = playerQueue.subList(0, max);

        playerQueue.removeAll(onProcessQueue(process));
    }

    @Override
    public QueueInfo getInfo() {
        long playersInMatch = 0;
        ArrayList<Long> matchIds = matches.get(queue());
        for (long id : matchIds) {
            Match match = MatchFactory.getCreator().findMatch(id);
            playersInMatch += match.team1().getTeamLength() + match.team2().getTeamLength();
        }

        return new QueueInfo(queue(), playerQueue.size(), playersInMatch, description(), allyCount(), opponentCount());
    }

    protected abstract List<String> onProcessQueue(List<String> queueToProcess);

    public NetworkMatch createMatch(String user1, String user2) throws IOException {
        Player player1 = PlayerFactory.getCreator().findPlayerByUUID(user1);
        Player player2 = PlayerFactory.getCreator().findPlayerByUUID(user2);
        long id = Global.SQL.getStoredMatchCount() + MatchFactory.getCreator().getAllActiveMatches().size();

        Team team1 = new Team(1, player1);
        Team team2 = new Team(2, player2);

        String map = MAPS[Global.RANDOM.nextInt(MAPS.length)];
        NetworkMatch match = MatchFactory.getCreator().createMatchFor(team1, team2, id, queue(), map, Main.TCP_UDP_SERVER);

        matches.get(queue()).add(match.getID());

        onTeamEnterMatch(match.getTeam1(), match.getTeam2());

        return match;
    }

    public NetworkMatch createMatch(Team team1, Team team2) throws IOException {
        long id = Global.SQL.getStoredMatchCount() + MatchFactory.getCreator().getAllActiveMatches().size();

        String map = MAPS[Global.RANDOM.nextInt(MAPS.length)];
        NetworkMatch match = MatchFactory.getCreator().createMatchFor(team1, team2, id, queue(), map, Main.TCP_UDP_SERVER);

        matches.get(queue()).add(match.getID());

        onTeamEnterMatch(team1, team2);

        ArrayHelper.assertTrueFor(team1.getTeamMembers(), new PFunction<PlayableEntity, Boolean>() {
            @Override
            public Boolean run(PlayableEntity p) {
                return p instanceof TestPlayer && ((TestPlayer) p).getQueue() == null;
            }
        }, "super.onTeamEnterMatch was not invoked!");

        return match;
    }

    public NetworkMatch createMatch(NetworkMatch match) throws IOException {
        long id = Global.SQL.getStoredMatchCount() + MatchFactory.getCreator().getAllActiveMatches().size();

        String map = MAPS[Global.RANDOM.nextInt(MAPS.length)];
        MatchFactory.getCreator().createMatchFor(match, id, queue(), map, Main.TCP_UDP_SERVER);

        matches.get(queue()).add(match.getID());

        onTeamEnterMatch(match.getTeam1(), match.getTeam2());

        ArrayHelper.assertTrueFor(match.getTeam1().getTeamMembers(), new PFunction<PlayableEntity, Boolean>() {
            @Override
            public Boolean run(PlayableEntity p) {
                return p instanceof TestPlayer && ((TestPlayer) p).getQueue() == null;
            }
        }, "super.onTeamEnterMatch was not invoked!");

        return match;
    }

    public void createMatch(NetworkMatch match, String map) throws IOException {
        long id = Global.SQL.getStoredMatchCount() + MatchFactory.getCreator().getAllActiveMatches().size();
        MatchFactory.getCreator().createMatchFor(match, id, queue(), map, Main.TCP_UDP_SERVER);

        matches.get(queue()).add(match.getID());

        onTeamEnterMatch(match.getTeam1(), match.getTeam2());

        ArrayHelper.assertTrueFor(match.getTeam1().getTeamMembers(), new PFunction<PlayableEntity, Boolean>() {
            @Override
            public Boolean run(PlayableEntity p) {
                return p instanceof TestPlayer && ((TestPlayer) p).getQueue() == null;
            }
        }, "super.onTeamEnterMatch was not invoked!");
    }

    protected void onTeamEnterMatch(Team team1, Team team2) {
        for (PlayableEntity p : team1.getTeamMembers()) {
            if (p instanceof TestPlayer)
                ((TestPlayer)p).setQueue(null);
        }

        for (PlayableEntity p : team2.getTeamMembers()) {
            if (p instanceof TestPlayer)
                ((TestPlayer)p).setQueue(null);
        }
    }
}
