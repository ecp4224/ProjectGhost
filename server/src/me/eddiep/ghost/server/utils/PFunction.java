package me.eddiep.ghost.server.utils;

public interface PFunction<T, R> {
    R run(T val);
}
