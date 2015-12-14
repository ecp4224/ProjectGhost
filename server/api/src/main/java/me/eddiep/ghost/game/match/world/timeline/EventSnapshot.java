package me.eddiep.ghost.game.match.world.timeline;

import me.eddiep.ghost.game.match.Event;
import me.eddiep.ghost.game.match.entities.Entity;

public class EventSnapshot {
    private short eventId;
    private short causeId;

    public static EventSnapshot createEvent(Event event, Entity cause) {
        EventSnapshot snapshot = new EventSnapshot();
        snapshot.eventId = event.getID();
        snapshot.causeId = cause.getID();

        return snapshot;
    }

    private EventSnapshot() { }

    public short getEventId() {
        return eventId;
    }

    public short getCauseId() {
        return causeId;
    }
}
