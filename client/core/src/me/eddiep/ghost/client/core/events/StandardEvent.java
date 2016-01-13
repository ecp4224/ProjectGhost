package me.eddiep.ghost.client.core.events;

import me.eddiep.ghost.client.core.Entity;
import me.eddiep.ghost.client.core.sprites.effects.Effect;
import me.eddiep.ghost.client.utils.NetworkUtils;
import org.jetbrains.annotations.NotNull;

public enum StandardEvent implements Event {
    FireGun(0) {
        @Override
        public void trigger(@NotNull Entity cause, double direction) {

        }
    },
    FireBoomerang(1) {
        @Override
        public void trigger(@NotNull Entity cause, double direction) {

        }
    },
    BoomerangCatch(2) {
        @Override
        public void trigger(@NotNull Entity cause, double direction) {

        }
    },
    FireCircle(3) {
        @Override
        public void trigger(@NotNull Entity cause, double direction) {
            byte[] temp = NetworkUtils.double2ByteArray(direction);
            float targetX = NetworkUtils.byteArray2Float(new byte[] { temp[0], temp[1], temp[2], temp[3] });
            float targetY = NetworkUtils.byteArray2Float(new byte[] { temp[4], temp[5], temp[6], temp[7] });

            Effect.EFFECTS[2].begin(700 + 600, 64, targetX, targetY, 700);
        }
    },
    LaserCharge(4) {
        @Override
        public void trigger(@NotNull Entity cause, double direction) {
            float cx = (float) (cause.getCenterX() + (Math.cos(direction) * (32 / 2f)));
            float cy = (float) (cause.getCenterY() + (Math.sin(direction) * (32 / 2f)));
            Effect.EFFECTS[0].begin(900, 48, cx, cy, direction);
        }
    },
    FireLaser(5) {
        @Override
        public void trigger(@NotNull Entity cause, double direction) {
            Effect.EFFECTS[1].begin(500, 20, cause.getCenterX(), cause.getCenterY(), direction);
        }
    },
    ItemPickUp(6) {
        @Override
        public void trigger(@NotNull Entity cause, double direction) {

        }
    },
    FireDash(7) {
        @Override
        public void trigger(@NotNull Entity cause, double direction) {

        }
    },
    PlayerHit(8) {
        @Override
        public void trigger(@NotNull Entity cause, double direction) {

        }
    },
    PlayerDeath(9) {
        @Override
        public void trigger(@NotNull Entity cause, double direction) {

        }
    };

    private short id;
    StandardEvent(int id) {
        this.id = (short) id;
    }
    StandardEvent(short id) {
        this.id = id;
    }

    @Override
    public short getID() {
        return id;
    }
}
