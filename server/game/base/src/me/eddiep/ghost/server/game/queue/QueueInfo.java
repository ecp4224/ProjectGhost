package me.eddiep.ghost.server.game.queue;

import me.eddiep.ghost.server.game.Game;
import me.eddiep.ghost.server.game.MatchFactory;

public class QueueInfo {
    private long playersInQueue;
    private long playersInMatch;
    private byte type;
    private String name;
    private String description;
    private int allyCount;
    private int opponentCount;
    private boolean isRanked;

    public QueueInfo(Game game) {
        this.type = game.id();
        this.name = game.name();
        this.description = game.description();
        this.allyCount = game.playerQueueProcessor().allyCount();
        this.opponentCount = game.playerQueueProcessor().opponentCount();
        this.playersInMatch = MatchFactory.getPlayerCount();
        this.playersInQueue = game.playerQueueProcessor().playerCount();
        this.isRanked = game.isRanked();
    }

    public boolean isRanked() {
        return isRanked;
    }

    public String getDescription() {
        return description;
    }

    public long getPlayersInMatch() {
        return playersInMatch;
    }

    public long getPlayersInQueue() {
        return playersInQueue;
    }

    public byte getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public int getAllyCount() {
        return allyCount;
    }

    public int getOpponentCount() {
        return opponentCount;
    }
}
