package me.eddiep.ghost.server.network.dataserv.actions;

import me.eddiep.ghost.server.network.dataserv.CentralServer;

public interface Action<T> {

    public String name();

    public Class<T> dataClass();

    public void performAction(CentralServer client, T object);
}
