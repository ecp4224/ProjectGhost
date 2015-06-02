package me.eddiep.ghost.server.game.util;

import me.eddiep.ghost.server.game.entities.playable.impl.Player;

public class NotificationBuilder {
    private String title;
    private String description;
    private Player target;
    private long expires = -1;
    private boolean asRequest;

    public static NotificationBuilder newNotification(Player target) {
        validateParameter(target, "Target");
        return new NotificationBuilder(target);
    }

    private NotificationBuilder(Player target) { this.target = target; }

    private NotificationBuilder() { }

    private static void validateParameter(Object parameter, String name) {
        if (parameter == null)
            throw new IllegalArgumentException(name + " cannot be null!");
    }

    public NotificationBuilder title(String title) {
        validateParameter(title, "Title");
        this.title = title;
        return this;
    }

    public NotificationBuilder description(String description) {
        validateParameter(description, "Description");
        this.description = description;
        return this;
    }

    public NotificationBuilder expiresInSeconds(int seconds) {
        expires = seconds;
        return this;
    }

    public NotificationBuilder expiresInMinutes(int minutes) {
        expires = minutes;
        return this;
    }

    public NotificationBuilder expiresInHours(int hours) {
        expires = hours;
        return this;
    }

    public NotificationBuilder expires(long unixTime) {
        expires = unixTime;
        return this;
    }

    public NotificationBuilder asRequest() {
        asRequest = true;
        return this;
    }

    public NotificationBuilder asNotification() {
        asRequest = false;
        return this;
    }

    public Notification build() {
        if (target == null)
            throw new IllegalArgumentException("Target must not be null!");

        if (title == null)
            title = "Notification";
        if (description == null)
            description = "";
        if (expires == -1) {
            expires = System.currentTimeMillis() + 300000;
        } else if (expires < System.currentTimeMillis()) { //using seconds/minutes/hours
            expires = System.currentTimeMillis() + expires;
        }

        if (asRequest)
            return new Request(target, title, description, expires);
        else
            return new Notification(target, title, description);
    }

    public Request buildRequest() {
        asRequest = true;
        return (Request) build();
    }
}
