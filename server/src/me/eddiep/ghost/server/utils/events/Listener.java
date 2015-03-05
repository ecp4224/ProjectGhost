package me.eddiep.ghost.server.utils.events;

public abstract class Listener<T extends Event> {

    public abstract void raised(T args);
}
