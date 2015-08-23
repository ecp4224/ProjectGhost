package me.eddiep.ghost.network.notifications;

public interface Notifiable {
    void sendNewNotification(Notification notification);

    void removeRequest(Request request);
}
