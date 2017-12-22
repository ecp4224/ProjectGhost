package com.boxtrotstudio.ghost.game.match;

public enum Event {
    FireGun(0),
    FireBoomerang(1),
    BoomerangCatch(2),
    FireCircle(3),
    LaserCharge(4),
    FireLaser(5),
    ItemPickUp(6),
    DashCharge(7),
    FireDash(8),
    PlayerHit(9),
    PlayerDeath(10),
    TutorialStart(11),
    DidMove(12),
    DidFire(13),
    HitOnce(14),
    SpawnSpeed(15),
    ObtainSpeed(16),
    HitTwice(17),
    GunBegin(18),
    LivesReset(19),
    TeamWin(20),
    TeamLose(21),
    MatchWin(22),
    MatchLose(23),
    CancelAbility(24);

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