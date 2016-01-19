package me.eddiep.ghost.client.core.sound;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;

public enum Sounds {

    GUN_FIRE("sounds/gun.mp3"),
    ITEM_PICKUP("sounds/pickup.mp3"),
    PLAYER_HIT("sounds/hit.mp3"),
    PLAYER_DEATH("sounds/death.mp3");

    private static float masterVolume = 1f;

    private final Sound handle;

    Sounds(String path) {
        this.handle = Gdx.audio.newSound(Gdx.files.internal(path));
    }


    public static void play(Sounds sound) {
        play(sound, masterVolume);
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
        sound.handle.play(volume, pitch, pan);
    }


    /**
     * Gets the default master volume.
     *
     * @see #setMasterVolume(float)
     */
    public static float getMasterVolume() {
        return masterVolume;
    }

    /**
     * Sets the default master volume. This volume will then be used to play all other sounds, unless a
     * different value is specified in the {@link #play(Sounds)} method when playing the sound.
     *
     * @param masterVolume The new master volume. The value must be in the range [0, 1]
     */
    public static void setMasterVolume(float masterVolume) {
        if (masterVolume < 0f || masterVolume > 1f) {
            throw new IllegalArgumentException("The master volume is out of the range [0, 1]");
        }

        Sounds.masterVolume = masterVolume;
    }
}
