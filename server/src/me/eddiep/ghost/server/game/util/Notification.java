package me.eddiep.ghost.server.game.util;

import me.eddiep.ghost.server.Main;
import me.eddiep.ghost.server.game.entities.Player;

public class Notification {
    protected final transient Player target;
    protected final String title;
    protected final String description;
    private int id;

    Notification(Player target, String title, String description) {
        this.title = title;
        this.description = description;
        this.target = target;
        regenerateId();
    }

    public int getId() {
        return id;
    }

    public void regenerateId() {
        this.id = Main.RANDOM.nextInt();
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public Player getTarget() {
        return target;
    }

    public Notification send() {
        target.sendNewNotification(this);
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Notification that = (Notification) o;

        if (id != that.id) return false;
        if (!description.equals(that.description)) return false;
        if (!target.equals(that.target)) return false;
        if (!title.equals(that.title)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = target.hashCode();
        result = 31 * result + title.hashCode();
        result = 31 * result + description.hashCode();
        result = 31 * result + id;
        return result;
    }
}
