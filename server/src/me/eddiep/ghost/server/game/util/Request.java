package me.eddiep.ghost.server.game.util;

import me.eddiep.ghost.server.Main;
import me.eddiep.ghost.server.game.entities.Player;
import me.eddiep.ghost.server.utils.PRunnable;

public class Request {
    private final transient Player target;
    private final String title;
    private final String description;
    private int id;
    private final long expires;
    private boolean accepted;
    private boolean resposed;
    private PRunnable<Request> onRespose;

    Request(Player target, String title, String description, long expires) {
        this.target = target;
        this.title = title;
        this.description = description;
        this.expires = expires;
        this.id = Main.RANDOM.nextInt();
    }

    public boolean hasResponded() {
        return resposed;
    }

    public int getId() {
        return id;
    }

    public void regenerateId() {
        this.id = Main.RANDOM.nextInt();
    }

    public boolean accepted() {
        return accepted;
    }

    public void respond(boolean respose) {
        if (System.currentTimeMillis() >= expires)
            return;

        this.accepted = respose;
        resposed = true;

        if (onRespose != null)
            onRespose.run(this);

        target.removeRequest(this);
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

    public long getExpires() {
        return expires;
    }

    public void onResponse(PRunnable<Request> callback) {
        onRespose = callback;
    }

    public void send() {
        target.sendNewRequest(this);
    }

    public boolean expired() {
        return System.currentTimeMillis() >= expires;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Request request = (Request) o;

        if (accepted != request.accepted) return false;
        if (expires != request.expires) return false;
        if (id != request.id) return false;
        if (resposed != request.resposed) return false;
        if (!description.equals(request.description)) return false;
        if (onRespose != null ? !onRespose.equals(request.onRespose) : request.onRespose != null) return false;
        if (!target.equals(request.target)) return false;
        if (!title.equals(request.title)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return id;
    }
}
