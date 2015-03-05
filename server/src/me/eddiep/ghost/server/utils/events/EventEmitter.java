package me.eddiep.ghost.server.utils.events;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public abstract class EventEmitter {
    private HashMap<String, ArrayList<Runnable>> events = new HashMap<>();

    public EventEmitter on(String event, Runnable r) {
        if (!events.containsKey(event))
            throw new IllegalArgumentException("No such event \"" + event + "\" !");

        events.get(event).add(r);
        return this;
    }

    public EventEmitter off(String event, Runnable r) {
        if (!events.containsKey(event))
            throw new IllegalArgumentException("No such event \"" + event + "\" !");

        events.get(event).remove(r);
        return this;
    }


    protected void emit(String event) {
        if (!events.containsKey(event))
            throw new IllegalArgumentException("No such event registered: \"" + event + "\" !");

        List<Runnable> temp = events.get(event);
        Runnable[] runnables = temp.toArray(new Runnable[temp.size()]);

        for (Runnable r : runnables) {
            r.run();
        }
    }

    protected void register(String event) {
        if (events.containsKey(event))
            return;

        events.put(event, new ArrayList<Runnable>());
    }
}
