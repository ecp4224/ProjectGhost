package com.boxtrotstudio.ghost.game.match.entities;

public class BaseTypeableEntity extends BaseEntity implements TypeableEntity {
    private final short type;

    public BaseTypeableEntity(short type) {
        this.type = type;
        setAlpha(1f);
    }

    public BaseTypeableEntity(int type) {
        this((short)type);
    }

    @Override
    public short getType() {
        return type;
    }
}
