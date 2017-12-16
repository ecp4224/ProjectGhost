package com.boxtrotstudio.ghost.client.core.game.events;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.boxtrotstudio.ghost.client.Ghost;
import com.boxtrotstudio.ghost.client.core.game.Entity;
import com.boxtrotstudio.ghost.client.core.game.SpriteEntity;
import com.boxtrotstudio.ghost.client.core.game.animations.Animation;
import com.boxtrotstudio.ghost.client.core.game.animations.AnimationType;
import com.boxtrotstudio.ghost.client.core.game.sprites.NetworkPlayer;
import com.boxtrotstudio.ghost.client.core.game.sprites.effects.Effect;
import com.boxtrotstudio.ghost.client.core.logic.Handler;
import com.boxtrotstudio.ghost.client.core.render.Text;
import com.boxtrotstudio.ghost.client.core.sound.Sounds;
import com.boxtrotstudio.ghost.client.handlers.GameHandler;
import com.boxtrotstudio.ghost.client.handlers.scenes.SpriteScene;
import com.boxtrotstudio.ghost.client.utils.Direction;
import com.boxtrotstudio.ghost.client.utils.NetworkUtils;
import com.boxtrotstudio.ghost.client.utils.Vector2f;
import org.jetbrains.annotations.NotNull;

public enum StandardEvent implements Event {
    FireGun(0) {
        @Override
        public void trigger(@NotNull final Entity entity, final double direction, @NotNull SpriteScene world) {
            if (!(entity instanceof SpriteEntity))
                return;
            SpriteEntity cause = (SpriteEntity)entity;

            Sounds.play(Sounds.GUN_FIRE);
            cause.getAnimation(AnimationType.SHOOT, Direction.fromRadians(direction))
                    .reset().play()
                    .onComplete(() -> {
                ((NetworkPlayer)cause).setFiring(false);
                if (cause.getVelocity().lengthSquared() == 0f) {
                    cause.getAnimation(AnimationType.READYGUN, Direction.fromRadians(direction))
                            .setFrame(Animation.LAST_FRAME)
                            .reverse()
                            .onCompletePlay(AnimationType.IDLE);
                }
            });

            if (cause instanceof NetworkPlayer) {
                ((NetworkPlayer)cause).setLastDirection(Direction.fromRadians(direction));
            }
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
        public void trigger(@NotNull Entity entity, double direction, @NotNull SpriteScene world) {
            if (!(entity instanceof SpriteEntity))
                return;
            SpriteEntity cause = (SpriteEntity)entity;
            cause.getAnimation(AnimationType.READYGUN, Direction.fromRadians(direction)).reset().play().holdOnComplete();

            float cx = (float) (cause.getCenterX() + (Math.cos(direction) * (32 / 2f)));
            float cy = (float) (cause.getCenterY() + (Math.sin(direction) * (32 / 2f)));

            double offsetX = cause.getCurrentAnimation().getData("particleOffsetX");
            double offsetY = cause.getCurrentAnimation().getData("particleOffsetY");

            cx += offsetX;
            cy += offsetY;

            Effect.EFFECTS[0].begin(200, 48, cx, cy, direction, world);

            Sounds.play(Sounds.LASER_CHARGE);

            ((NetworkPlayer) cause).setFiring(true);

            ((NetworkPlayer)cause).setLastDirection(Direction.fromRadians(direction));
        }
    },
    FireLaser(5) {
        @Override
        public void trigger(@NotNull final Entity entity, final double direction, @NotNull SpriteScene world) {
            if (!(entity instanceof SpriteEntity))
                return;
            SpriteEntity cause = (SpriteEntity)entity;

            //We need to get the offset data from the current animation (which should always be READYGUN)
            //Because the SHOOT animation does not hold this information
            double offsetX = cause.getCurrentAnimation().getData("particleOffsetX");
            double offsetY = cause.getCurrentAnimation().getData("particleOffsetY");

            cause.getAnimation(AnimationType.SHOOT, Direction.fromRadians(direction))
                    .reset().play()
                    .onComplete(() -> {
                ((NetworkPlayer)cause).setFiring(false);
                if (cause.getVelocity().lengthSquared() == 0f) {
                    cause.getAnimation(AnimationType.READYGUN, Direction.fromRadians(direction))
                            .setFrame(Animation.LAST_FRAME)
                            .reverse()
                            .onCompletePlay(AnimationType.IDLE);
                }
            }).executeOnFrame(2, () -> {
                float cx = cause.getCenterX(), cy = cause.getCenterY();

                cx += offsetX;
                cy += offsetY;

                Effect.EFFECTS[1].begin(500, 20, cx, cy, direction, world);

                Sounds.play(Sounds.FIRE_LASER);
            });


            if (cause instanceof NetworkPlayer) {
                ((NetworkPlayer)cause).setLastDirection(Direction.fromRadians(direction));
            }
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
            float cx = (float) (cause.getCenterX() + (Math.cos(direction) * (32 / 2f)));
            float cy = (float) (cause.getCenterY() + (Math.sin(direction) * (32 / 2f)));
            Effect.EFFECTS[0].begin(800, 200, cx, cy, direction, world);
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

        }
    },
    PlayerDeath(10) {
        @Override
        public void trigger(@NotNull Entity entity, double direction, @NotNull SpriteScene world) {
            if (!(entity instanceof SpriteEntity))
                return;
            SpriteEntity cause = (SpriteEntity)entity;

            cause.setVelocity(new Vector2f(0f, 0f));

            Sounds.play(Sounds.PLAYER_DEATH);
            Animation animation = cause.getAnimation(AnimationType.DEATH, Direction.fromRadians(direction));
            if (animation != null)
                animation.reset().play().holdOnComplete();
        }
    },

    /*
        Tutorial Events
     */

    TutorialStart(11) {
        @Override
        public void trigger(@NotNull Entity cause, double duration, @NotNull SpriteScene world) {
            Ghost.tutorialText = new Text(24, new Color(1f, 1f, 1f, 1f), Gdx.files.internal("fonts/TitilliumWeb-Regular.ttf"));
            float widthMult = (Gdx.graphics.getWidth() / 1280f);
            float heightMult = (Gdx.graphics.getHeight() / 720f);
            Ghost.tutorialText.setX((1280 / 2) * widthMult);
            Ghost.tutorialText.setY(130 * heightMult);
            Ghost.tutorialText.setText("To get started, try to move around.\nUse W A S D, or click where you want to go.");
            world.addEntity(Ghost.tutorialText);
        }
    },

    DidMove(12) {
        @Override
        public void trigger(@NotNull Entity cause, double duration, @NotNull SpriteScene world) {
            Ghost.tutorialText.setText("Good! Now, press the Right Mouse Button to fire your weapon.\nFiring a weapon reveals your position to your opponent. Try it out.");
        }
    },

    DidFire(13) {
        @Override
        public void trigger(@NotNull Entity cause, double duration, @NotNull SpriteScene world) {
            Ghost.tutorialText.setText("Note that your opponent just revealed his position.\nUse this opportunity to adjust your aim.");
        }
    },

    HitOnce(14) {
        @Override
        public void trigger(@NotNull Entity cause, double duration, @NotNull SpriteScene world) {
            Ghost.tutorialText.setText("Nice shot!\nYou'll need to land two more hits to win.");
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
            Ghost.tutorialText.setText("Items you pick up are stored in your inventory. Press Q to use your Health Boost.");
        }
    },

    HitTwice(17) {
        @Override
        public void trigger(@NotNull Entity cause, double duration, @NotNull SpriteScene world) {
            Ghost.tutorialText.setText("Your opponent is almost defeated. Bring it on home!");
        }
    },

    GunBegin(18) {
        @Override
        public void trigger(@NotNull Entity entity, double direction, @NotNull SpriteScene world) {
            if (!(entity instanceof SpriteEntity))
                return;
            SpriteEntity cause = (SpriteEntity)entity;

            ((NetworkPlayer) cause).setFiring(true);
            cause.getAnimation(AnimationType.READYGUN, Direction.fromRadians(direction)).reset().play();
        }
    },

    LivesReset(19) {
        @Override
        public void trigger(@NotNull Entity entity, double direction, @NotNull SpriteScene world) {
            if (!(entity instanceof SpriteEntity))
                return;
            SpriteEntity cause = (SpriteEntity)entity;
            cause.getAnimation(AnimationType.DEATH, cause.getCurrentAnimation().getDirection())
                    .setFrame(Animation.LAST_FRAME)
                    .reverse()
                    .onCompletePlay(AnimationType.IDLE);
        }
    },

    TeamWin(20) {
        @Override
        public void trigger(@NotNull Entity cause, double direction, @NotNull SpriteScene world) {
            if (cause.getID() != 0)
                return; //This event is not for us, ignore..

            Handler h = Ghost.getInstance().getHandler();

            if (h instanceof GameHandler) {
                GameHandler game = (GameHandler)h;

                switch ((int)direction) {
                    case 1:
                        game.setDidWin1(1);
                        break;
                    case 2:
                        game.setDidWin2(1);
                        break;
                    case 3:
                        game.setDidWin3(1);
                        break;
                }

                System.err.println(game.getDidWin1() + " : " + game.getDidWin2() + " : " + game.getDidWin3());
            }
        }
    },

    TeamLose(21) {
        @Override
        public void trigger(@NotNull Entity cause, double direction, @NotNull SpriteScene world) {
            if (cause.getID() != 0)
                return; //This event is not for us, ignore..

            Handler h = Ghost.getInstance().getHandler();

            if (h instanceof GameHandler) {
                GameHandler game = (GameHandler)h;

                switch ((int)direction) {
                    case 1:
                        game.setDidWin1(0);
                        break;
                    case 2:
                        game.setDidWin2(0);
                        break;
                    case 3:
                        game.setDidWin3(0);
                        break;
                }

                System.err.println(game.getDidWin1() + " : " + game.getDidWin2() + " : " + game.getDidWin3());
            }
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
