package com.boxtrotstudio.ghost.client.core.game.timeline;

public class EventSnapshot {
    private short eventId;
    private short causeId;
    private double direction;

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
