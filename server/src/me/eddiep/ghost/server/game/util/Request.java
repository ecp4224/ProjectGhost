package me.eddiep.ghost.server.game.util;

import me.eddiep.ghost.server.game.entities.Player;
import me.eddiep.ghost.server.utils.PRunnable;

public class Request {
    private final transient Player target;
    private final String title;
    private final String description;
    private final long expires;
    private boolean accepted;
    private boolean resposed;
    private PRunnable<Request> onRespose;

    Request(Player target, String title, String description, long expires) {
        this.target = target;
        this.title = title;
        this.description = description;
        this.expires = expires;
    }

    public boolean hasResponded() {
        return resposed;
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
}
