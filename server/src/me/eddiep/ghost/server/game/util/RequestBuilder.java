package me.eddiep.ghost.server.game.util;

import me.eddiep.ghost.server.game.entities.Player;

public class RequestBuilder {
    private String title;
    private String description;
    private Player target;
    private long expires = -1;

    public static RequestBuilder newRequest(Player target) {
        return new RequestBuilder(target);
    }

    private RequestBuilder(Player target) { this.target = target; }

    private RequestBuilder() { }

    public RequestBuilder title(String title) {
        this.title = title;
        return this;
    }

    public RequestBuilder description(String description) {
        this.description = description;
        return this;
    }

    public RequestBuilder expiresInSeconds(int seconds) {
        expires = seconds;
        return this;
    }

    public RequestBuilder expiresInMinutes(int minutes) {
        expires = minutes;
        return this;
    }

    public RequestBuilder expiresInHours(int hours) {
        expires = hours;
        return this;
    }

    public RequestBuilder expires(long unixTime) {
        expires = unixTime;
        return this;
    }

    public Request build() {
        if (target == null)
            throw new IllegalArgumentException("Target must not be null!");

        if (title == null)
            title = "Request";
        if (description == null)
            description = "";
        if (expires == -1) {
            expires = System.currentTimeMillis() + 300000;
        } else if (expires < System.currentTimeMillis()) { //using seconds/minutes/hours
            expires = System.currentTimeMillis() + expires;
        }

        return new Request(target, title, description, expires);
    }
}
