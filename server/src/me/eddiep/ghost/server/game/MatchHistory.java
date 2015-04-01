package me.eddiep.ghost.server.game;

import me.eddiep.ghost.server.game.entities.OfflineTeam;
import me.eddiep.ghost.server.game.queue.Queues;
import org.bson.Document;

public class MatchHistory implements Match {

    private long id;
    private OfflineTeam team1, team2;
    private int winningTeam, losingTeam;
    private long matchStarted, matchEnded;
    private Queues type;

    MatchHistory(ActiveMatch match) {
        this.id = match.getID();
        this.team1 = match.team1();
        this.team2 = match.team2();
        this.winningTeam = match.winningTeam() == null ? -1 : match.winningTeam().getTeamNumber();
        this.losingTeam = match.losingTeam() == null ? -1 : match.losingTeam().getTeamNumber();
        this.matchStarted = match.getMatchStarted();
        this.matchEnded = match.getMatchEnded();
        this.type = match.queueType();
    }

    private MatchHistory() { }

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
        return type;
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
                .append("type", (int)type.asByte());
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

        MatchHistory history = new MatchHistory();
        history.id = id;
        history.team1 = team1;
        history.team2 = team2;
        history.winningTeam = winningTeam;
        history.losingTeam = losingTeam;
        history.matchStarted = matchStart;
        history.matchEnded = matchEnd;
        history.type = Queues.byteToType(type);

        return history;
    }
}
