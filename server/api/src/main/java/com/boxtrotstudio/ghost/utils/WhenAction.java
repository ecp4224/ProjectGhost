package com.boxtrotstudio.ghost.utils;

import java.util.ArrayList;

public class WhenAction<T> {
    private PFunction<T, Boolean> condition;
    private T object;
    private ArrayList<PRunnable<T>> actions = new ArrayList<>();
    private ArrayList<PRunnable<T>> alwaysActions = new ArrayList<>();

    public static <T> WhenAction<T> when(T object, PFunction<T, Boolean> condition) {
        return new WhenAction<>(object, condition);
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

    public WhenAction<T> alwaysExecute(PRunnable<T> action) {
        this.alwaysActions.add(action);

        return this;
    }

    public WhenAction<T> alwaysExecute(Runnable action) {
        this.alwaysActions.add(PRunnable.wrap(action));

        return this;
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
        if (actions.size() == 0 && alwaysActions.size() == 0)
            return true; //Nothing to do, stop checking

        if (condition.run(object)) {
            for (PRunnable<T> action : actions) {
                try {
                    action.run(object);
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }
            actions.clear();

            if (alwaysActions.size() > 0) {
                for (PRunnable<T> action: alwaysActions) {
                    try {
                        action.run(object);
                    } catch (Throwable t) {
                        t.printStackTrace();
                    }
                }

                return false;
            }
            return true;
        }

        return false;
    }
}
