package me.eddiep.ghost.client.core.game.events;

import me.eddiep.ghost.client.core.game.Entity;
import me.eddiep.ghost.client.core.render.scene.impl.SpriteScene;
import me.eddiep.ghost.client.core.sound.Sounds;
import me.eddiep.ghost.client.core.game.sprites.NetworkPlayer;
import me.eddiep.ghost.client.core.game.sprites.effects.Effect;
import me.eddiep.ghost.client.utils.NetworkUtils;
import org.jetbrains.annotations.NotNull;

public enum StandardEvent implements Event {
    FireGun(0) {
        @Override
        public void trigger(@NotNull Entity cause, double direction, @NotNull SpriteScene world) {
            Sounds.play(Sounds.GUN_FIRE);
        }
    },
    FireBoomerang(1) {
        @Override
        public void trigger(@NotNull Entity cause, double direction, @NotNull SpriteScene world) {

        }
    },
    BoomerangCatch(2) {
        @Override
        public void trigger(@NotNull Entity cause, double direction, @NotNull SpriteScene world) {

        }
    },
    FireCircle(3) {
        @Override
        public void trigger(@NotNull Entity cause, double direction, @NotNull SpriteScene world) {
            byte[] temp = NetworkUtils.double2ByteArray(direction);
            float targetX = NetworkUtils.byteArray2Float(new byte[] { temp[0], temp[1], temp[2], temp[3] });
            float targetY = NetworkUtils.byteArray2Float(new byte[] { temp[4], temp[5], temp[6], temp[7] });

            Effect.EFFECTS[2].begin(700 + 600, 64, targetX, targetY, 700, world);
        }
    },
    LaserCharge(4) {
        @Override
        public void trigger(@NotNull Entity cause, double direction, @NotNull SpriteScene world) {
            float cx = (float) (cause.getCenterX() + (Math.cos(direction) * (32 / 2f)));
            float cy = (float) (cause.getCenterY() + (Math.sin(direction) * (32 / 2f)));
            Effect.EFFECTS[0].begin(900, 48, cx, cy, direction, world);

            Sounds.play(Sounds.LASER_CHARGE);
        }
    },
    FireLaser(5) {
        @Override
        public void trigger(@NotNull Entity cause, double direction, @NotNull SpriteScene world) {
            Effect.EFFECTS[1].begin(500, 20, cause.getCenterX(), cause.getCenterY(), direction, world);

            Sounds.play(Sounds.FIRE_LASER);
        }
    },
    ItemPickUp(6) {
        @Override
        public void trigger(@NotNull Entity cause, double direction, @NotNull SpriteScene world) {
            Sounds.play(Sounds.ITEM_PICKUP);
        }
    },
    DashCharge(7) {
        @Override
        public void trigger(@NotNull Entity cause, double direction, @NotNull SpriteScene world) {

        }
    },
    FireDash(8) {
        @Override
        public void trigger(@NotNull Entity cause, double direction, @NotNull SpriteScene world) {

        }
    },
    PlayerHit(9) {
        @Override
        public void trigger(@NotNull Entity cause, double direction, @NotNull SpriteScene world) {
            if (!((NetworkPlayer) cause).getDead()) {
                Sounds.play(Sounds.PLAYER_HIT);
            }
        }
    },
    PlayerDeath(10) {
        @Override
        public void trigger(@NotNull Entity cause, double direction, @NotNull SpriteScene world) {
            Sounds.play(Sounds.PLAYER_DEATH);
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
