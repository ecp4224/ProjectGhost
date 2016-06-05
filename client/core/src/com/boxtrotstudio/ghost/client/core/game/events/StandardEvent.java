package com.boxtrotstudio.ghost.client.core.game.events;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.async.ThreadUtils;
import com.boxtrotstudio.ghost.client.Ghost;
import com.boxtrotstudio.ghost.client.core.game.Entity;
import com.boxtrotstudio.ghost.client.core.game.sprites.NetworkPlayer;
import com.boxtrotstudio.ghost.client.core.game.sprites.effects.Effect;
import com.boxtrotstudio.ghost.client.core.render.Text;
import com.boxtrotstudio.ghost.client.core.render.scene.Scene;
import com.boxtrotstudio.ghost.client.core.sound.Sounds;
import com.boxtrotstudio.ghost.client.handlers.scenes.SpriteScene;
import com.boxtrotstudio.ghost.client.utils.NetworkUtils;
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
    },

    /*
        Tutorial Events
     */

    TutorialStart(11) {
        @Override
        public void trigger(@NotNull Entity cause, double duration, @NotNull SpriteScene world) {
            Ghost.tutorialText = new Text(24, new Color(1f, 1f, 1f, 1f), Gdx.files.internal("fonts/TitilliumWeb-Regular.ttf"));
            Ghost.tutorialText.setX(1280 / 2);
            Ghost.tutorialText.setY(130);
            Ghost.tutorialText.setText("To get started, try to move around. \nClick where you want to go to direct your player there.");
            world.addEntity(Ghost.tutorialText);
        }
    },

    DidMove(12) {
        @Override
        public void trigger(@NotNull Entity cause, double duration, @NotNull SpriteScene world) {
            Ghost.tutorialText.setText("Good! Now, press the Right Mouse Button to fire your weapon. \nFiring a weapon reveals your position to your opponent. Try it out.");
        }
    },

    DidFire(13) {
        @Override
        public void trigger(@NotNull Entity cause, double duration, @NotNull SpriteScene world) {
            Ghost.tutorialText.setText("Note that your opponent just revealed his position. \nUse this opportunity to adjust your aim.");
        }
    },

    HitOnce(14) {
        @Override
        public void trigger(@NotNull Entity cause, double duration, @NotNull SpriteScene world) {
            Ghost.tutorialText.setText("Nice shot!\n You'll need to land two more hits to win.");
        }
    },

    SpawnSpeed(15) {
        @Override
        public void trigger(@NotNull Entity cause, double duration, @NotNull SpriteScene world) {
            Ghost.tutorialText.setText("Hey look, an item! Try picking it up, but be careful; you might blow your cover.");
    }
    },

    ObtainSpeed(16) {
        @Override
        public void trigger(@NotNull Entity cause, double duration, @NotNull SpriteScene world) {
            Ghost.tutorialText.setText("Items you pick up are stored in your inventory. Press 1 to use your Speed Boost.");
        }
    },

    HitTwice(17) {
        @Override
        public void trigger(@NotNull Entity cause, double duration, @NotNull SpriteScene world) {
            Ghost.tutorialText.setText("Your opponent is almost defeated. Bring it on home!");
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
