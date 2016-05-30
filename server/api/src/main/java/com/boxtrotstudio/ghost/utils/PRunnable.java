package com.boxtrotstudio.ghost.utils;

/**
 * Represents a {@link java.lang.Runnable} that takes in a single parameter
 * @param <T> The type of the parameter
 */
public interface PRunnable<T> {
    void run(T p);
}
