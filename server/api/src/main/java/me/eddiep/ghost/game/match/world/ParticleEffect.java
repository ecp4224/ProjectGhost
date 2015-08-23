package me.eddiep.ghost.game.match.world;

public enum ParticleEffect {
    CHARGE(0),
    LINE(1);

    byte id;
    ParticleEffect(byte b) {
        this.id = b;
    }

    ParticleEffect(int i) {
        this((byte)i);
    }

    public byte getId() {
        return id;
    }

    public static ParticleEffect fromByte(byte id) {
        for (ParticleEffect effect : values()) {
            if (effect.getId() == id)
                return effect;
        }
        return null;
    }
}
