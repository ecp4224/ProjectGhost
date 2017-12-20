package com.boxtrotstudio.ghost.network.notifications;

import com.boxtrotstudio.ghost.utils.PRunnable;

public class Request extends Notification {
    private final long expires;
    private boolean accepted;
    private boolean responded;
    private PRunnable<Request> onResponse;

    Request(Notifiable target, String title, String description, long expires) {
        super(target, title, description);
        this.expires = expires;
    }

    public boolean hasResponded() {
        return responded;
    }

    public boolean accepted() {
        return accepted;
    }

    public void respond(boolean accepted) {
        if (System.currentTimeMillis() >= expires)
            return;

        this.accepted = accepted;
        responded = true;

        if (onResponse != null)
            onResponse.run(this);

        target.removeRequest(this);
    }

    public long getExpires() {
        return expires;
    }

    public Request onResponse(PRunnable<Request> callback) {
        onResponse = callback;
        return this;
    }

    public boolean expired() {
        return System.currentTimeMillis() >= expires;
    }
}
