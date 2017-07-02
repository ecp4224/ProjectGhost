package com.boxtrotstudio.ghost.client.core.sound;

import com.badlogic.gdx.audio.Sound;
import com.boxtrotstudio.ghost.client.Ghost;
import com.boxtrotstudio.ghost.client.utils.GlobalOptions;

public enum Sounds {

    GUN_FIRE("sounds/gun.mp3"),
    ITEM_PICKUP("sounds/pickup.mp3"),
    PLAYER_HIT("sounds/hit.mp3"),
    PLAYER_DEATH("sounds/death.mp3"),
    LASER_CHARGE("sounds/laser_charge.mp3"),
    FIRE_LASER("sounds/laser_fire.mp3");

    private final Sound handle;

    Sounds(String path) {
        this.handle = Ghost.ASSETS.get(path);
    }


    public static void play(Sounds sound) {
        float volume = GlobalOptions.getOptions().masterVolume() * GlobalOptions.getOptions().fxVolume();
        play(sound, volume);
    }

    public static void play(Sounds sound, float volume) {
        play(sound, volume, 1f, 0f);
    }

    /**
     * Plays a sound with the specified parameters.
     *
     * @param sound The sound to play.
     * @param volume The volume of the sound in the range [0, 1]
     * @param pitch The pitch of the sound in the range [0.5, 2]
     * @param pan The panning of the sound in the range [-1, 1]
     */
    public static void play(Sounds sound, float volume, float pitch, float pan) {
        //sound.handle.play(volume, pitch, pan);
    }
}
