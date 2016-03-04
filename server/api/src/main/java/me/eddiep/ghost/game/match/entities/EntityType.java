package me.eddiep.ghost.game.match.entities;

public enum EntityType {
    TEXT(-3),
    ITEM_SPAWN(-2),
    LIGHT(-1),
    TEAMMATE(0),
    ENEMY(1),
    BULLET(2),
    LASER(3),
    CIRCLE(4),
    BOOMERANG(5),
    BOOMERANG_LINE(6),
    SPEED_ITEM(10),
    HEALTH_ITEM(11),
    SHIELD_ITEM(12),
    INVISIBLE_ITEM(13),
    EMP_ITEM(14),
    JAM_ITEM(15),
    FIRERATE_ITEM(16),
    WALL(80),
    MIRROR(81),
    ONEWAY_MIRROR(82),
    RADIUS_SLOWFIELD(83),
    RECT_SLOWFIELD(84);


    short type;
    EntityType(int type) { this.type = (short)type; }

    public short getType() {
        return type;
    }
}
