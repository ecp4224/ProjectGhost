package me.eddiep.ghost.game.match;

public enum Event {
    FireGun(0),
    FireBoomerang(1),
    BoomerangCatch(2),
    FireCircle(3),
    LaserCharge(4),
    FireLaser(5),
    ItemPickUp(6),
    FireDash(7),
    PlayerHit(8),
    PlayerDeath(9);

    private short id;
    Event(int id) {
        this.id = (short) id;
    }
    Event(short id) {
        this.id = id;
    }

    public short getID() {
        return id;
    }
}
