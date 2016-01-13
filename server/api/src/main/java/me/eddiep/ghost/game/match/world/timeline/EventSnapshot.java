package me.eddiep.ghost.game.match.world.timeline;

import me.eddiep.ghost.game.match.Event;
import me.eddiep.ghost.game.match.entities.Entity;

public class EventSnapshot {
    private short eventId;
    private short causeId;
    private double direction;

    public static EventSnapshot createEvent(Event event, Entity cause, double direction) {
        EventSnapshot snapshot = new EventSnapshot();
        snapshot.eventId = event.getID();
        snapshot.causeId = cause.getID();
        snapshot.direction = direction;

        return snapshot;
    }

    private EventSnapshot() { }

    public double getDirection() {
        return direction;
    }

    public short getEventId() {
        return eventId;
    }

    public short getCauseId() {
        return causeId;
    }
}
