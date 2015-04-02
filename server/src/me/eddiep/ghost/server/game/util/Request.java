package me.eddiep.ghost.server.game.util;

import me.eddiep.ghost.server.Main;
import me.eddiep.ghost.server.game.entities.Player;
import me.eddiep.ghost.server.utils.PRunnable;

public class Request extends Notification {
    private final long expires;
    private boolean accepted;
    private boolean resposed;
    private PRunnable<Request> onRespose;

    Request(Player target, String title, String description, long expires) {
        super(target, title, description);
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

        target.removeRequest(this);
    }

    public long getExpires() {
        return expires;
    }

    public Request onResponse(PRunnable<Request> callback) {
        onRespose = callback;
        return this;
    }

    public boolean expired() {
        return System.currentTimeMillis() >= expires;
    }
}