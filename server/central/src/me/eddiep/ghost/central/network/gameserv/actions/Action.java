package me.eddiep.ghost.central.network.gameserv.actions;

import me.eddiep.ghost.central.network.gameserv.GameServerConnection;

public interface Action<T> {

    public String actionName();

    public Class<T> dataClass();

    public void performAction(GameServerConnection client, T object);
}
