package com.boxtrotstudio.ghost.client.utils;

/**
 * Represents a {@link Runnable} that takes in a single parameter
 * @param <T> The type of the parameter
 */
public interface PRunnable<T> {
    void run(T p);
}
