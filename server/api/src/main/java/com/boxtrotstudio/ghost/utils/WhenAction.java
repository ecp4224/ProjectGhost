package com.boxtrotstudio.ghost.utils;

import java.util.ArrayList;

public class WhenAction<T> {
    private PFunction<T, Boolean> condition;
    private T object;
    private ArrayList<PRunnable<T>> actions = new ArrayList<>();

    public static <T> WhenAction<T> when(T object, PFunction<T, Boolean> condition) {
        return new WhenAction<T>(object, condition);
    }

    private WhenAction(T object, PFunction<T, Boolean> condition) {
        this.object = object;
        this.condition = condition;
    }

    public PFunction<T, Boolean> getCondition() {
        return condition;
    }

    public T getObject() {
        return object;
    }

    public WhenAction<T> execute(PRunnable<T> action) {
        this.actions.add(action);

        return this;
    }

    public WhenAction<T> execute(Runnable action) {
        this.actions.add(PRunnable.wrap(action));

        return this;
    }

    public boolean check() {
        if (actions.size() == 0)
            return false;

        if (condition.run(object)) {
            for (PRunnable<T> action : actions) {
                try {
                    action.run(object);
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }
            return true;
        }

        return false;
    }
}
