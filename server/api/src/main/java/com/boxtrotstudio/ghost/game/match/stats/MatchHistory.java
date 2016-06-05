package com.boxtrotstudio.ghost.game.match.stats;

import com.boxtrotstudio.ghost.game.match.Match;
import com.boxtrotstudio.ghost.game.match.world.map.WorldMap;
import com.boxtrotstudio.ghost.game.match.world.timeline.Timeline;
import com.boxtrotstudio.ghost.game.match.LiveMatch;
import com.boxtrotstudio.ghost.game.queue.Queues;
import com.boxtrotstudio.ghost.game.team.OfflineTeam;
import org.bson.Document;

import java.util.List;

public class MatchHistory implements Match {

    private long id;
    private WorldMap map;
    private OfflineTeam team1, team2;
    private int winningTeam, losingTeam;
    private long matchStarted, matchEnded;
    private Timeline timeline;
    private byte queue;

    public MatchHistory(LiveMatch match) {
        this.id = match.getID();
        this.team1 = match.team1();
        this.team2 = match.team2();
        this.winningTeam = match.winningTeam() == null ? -1 : match.winningTeam().getTeamNumber();
        this.losingTeam = match.losingTeam() == null ? -1 : match.losingTeam().getTeamNumber();
        this.matchStarted = match.getMatchStarted();
        this.matchEnded = match.getMatchEnded();
        this.queue = match.queueType().asByte();
        this.map = match.getWorld().getWorldMap();

        timeline = match.getWorld().getTimeline();
    }

    private MatchHistory() { }

    public Timeline getTimeline() {
        return timeline;
    }

    @Override
    public long getID() {
        return id;
    }

    @Override
    public OfflineTeam team1() {
        return team1;
    }

    @Override
    public OfflineTeam team2() {
        return team2;
    }

    @Override
    public OfflineTeam winningTeam() {
        return team1.getTeamNumber() == winningTeam ? team1 : team2.getTeamNumber() == winningTeam ? team2 : null;
    }

    @Override
    public OfflineTeam losingTeam() {
        return team1.getTeamNumber() == losingTeam ? team1 : team2.getTeamNumber() == losingTeam ? team2 : null;
    }

    @Override
    public long getMatchStarted() {
        return matchStarted;
    }

    @Override
    public long getMatchEnded() {
        return matchEnded;
    }

    @Override
    public Queues queueType() {
        return Queues.byteToType(queue);
    }

    public WorldMap getMap() {
        return map;
    }

    public Document asDocument() {

        return new Document()
                .append("id", id)
                .append("team1", team1.asDocument())
                .append("team2", team2.asDocument())
                .append("winningTeam", winningTeam)
                .append("losingTeam", losingTeam)
                .append("matchStart", matchStarted)
                .append("matchEnded", matchEnded)
                .append("type", (int)queue);
    }

    public static MatchHistory fromDocument(Document document) {
        long id = document.getLong("id");
        OfflineTeam team1 = OfflineTeam.fromDocument(document.get("team1", Document.class));
        OfflineTeam team2 = OfflineTeam.fromDocument(document.get("team2", Document.class));
        int winningTeam = document.getInteger("winningTeam");
        int losingTeam = document.getInteger("losingTeam");
        long matchStart = document.getLong("matchStart");
        long matchEnd = document.getLong("matchEnded");
        byte type = document.getInteger("type").byteValue();
        List<Document> documents = document.get("stats", List.class);

        MatchHistory history = new MatchHistory();
        history.id = id;
        history.team1 = team1;
        history.team2 = team2;
        history.winningTeam = winningTeam;
        history.losingTeam = losingTeam;
        history.matchStarted = matchStart;
        history.matchEnded = matchEnd;
        history.queue = type;

        return history;
    }
}
