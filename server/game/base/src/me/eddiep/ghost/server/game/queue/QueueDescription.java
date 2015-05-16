package me.eddiep.ghost.server.game.queue;

import me.eddiep.ghost.server.game.Game;

public class QueueDescription {
    private byte type;
    private byte catagoryType;
    private String name;
    private String description;
    private boolean isRanked;

    public QueueDescription(Game game) {
        this.type = game.id();
        this.catagoryType = game.categoryId();
        this.name = game.name();
        this.description = game.description();
        this.isRanked = game.isRanked();
    }

    public byte getType() {
        return type;
    }

    public byte getCatagoryType() {
        return catagoryType;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public boolean isRanked() {
        return isRanked;
    }
}
