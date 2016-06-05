package com.boxtrotstudio.ghost.network.notifications;

public interface Notifiable {
    void sendNewNotification(Notification notification);

    void removeRequest(Request request);
}
