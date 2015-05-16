package me.eddiep.ghost.server.utils;

public interface PFunction<T, R> {
    public R run(T val);
}
