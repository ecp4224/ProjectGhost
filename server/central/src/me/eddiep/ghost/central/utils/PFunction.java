package me.eddiep.ghost.central.utils;

public interface PFunction<T, R> {
    public R run(T val);
}
