package com.boxtrotstudio.ghost.client.core.sound;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.boxtrotstudio.ghost.client.utils.GlobalOptions;
import com.boxtrotstudio.ghost.client.utils.Timer;

public enum Songs {
    LOADING("sounds/music/loading.mp3"),
    FIGHT("sounds/music/fight.mp3"),
    LOST("sounds/music/lost.mp3"),
    LOST_MENU("sounds/music/lost-continued.mp3"),
    MENU("sounds/music/menu.mp3"),
    QUEUE("sounds/music/queue.mp3"),
    VICTORY("sounds/music/victory.mp3");

    private Music handle;
    private String path;
    private boolean disposed;
    Songs(String path) {
        this.path = path;
    }

    public float getPosition() {
        if (isDisposed())
            return -1f;

        return handle.getPosition();
    }

    public float getVolume() {
        if (isDisposed())
            return -1f;

        return handle.getVolume();
    }

    public boolean isLooping() {
        if (isDisposed())
            return false;

        return handle.isLooping();
    }

    public Songs pause() {
        if (isDisposed())
            return this;

        handle.pause();
        return this;
    }

    public Songs play() {
        if (isDisposed()) {
            createHandle();
        }

        handle.play();
        return this;
    }

    public boolean isPlaying() {
        return !isDisposed() && handle.isPlaying();

    }

    public Songs setLooping(boolean loop) {
        if (isDisposed())
            return this;

        handle.setLooping(loop);
        return this;
    }

    public Songs setOnCompletionListener(Music.OnCompletionListener listener) {
        if (isDisposed())
            return this;

        handle.setOnCompletionListener(listener);
        return this;
    }

    public Songs setPan(float pan, float volume) {
        if (isDisposed())
            return this;

        handle.setPan(pan, volume);
        return this;
    }

    public Songs setPosition(float position) {
        if (isDisposed())
            return this;

        handle.setPosition(position);
        return this;
    }

    public Songs setVolume(float volume) {
        if (isDisposed() || !isPlaying())
            return this;

        handle.setVolume(volume);
        return this;
    }

    public Songs stop() {
        if (isDisposed())
            return this;

        handle.stop();
        dispose();
        return this;
    }

    public Songs fadeIn() {
        return fadeIn(800f);
    }

    public Songs fadeOut() {
        return fadeOut(800f);
    }

    public Songs fadeIn(Runnable onFaded) {
        return fadeIn(800f, onFaded);
    }

    public Songs fadeOut(Runnable onFaded) {
        return fadeOut(800f, onFaded);
    }

    public Songs fadeIn(float duration) {
        float startVolume = 0f;
        float endVolume = GlobalOptions.getOptions().musicVolume();

        return fade(startVolume, endVolume, duration, null);
    }

    public Songs fadeOut(float duration) {
        float startVolume = getVolume();
        float endVolume = 0f;

        return fade(startVolume, endVolume, duration, this::stop);
    }

    public Songs fadeIn(float duration, Runnable onFaded) {
        float startVolume = 0f;
        float endVolume = GlobalOptions.getOptions().musicVolume();

        return fade(startVolume, endVolume, duration, onFaded);
    }

    public Songs fadeOut(float duration, Runnable onFaded) {
        float startVolume = getVolume();
        float endVolume = 0f;

        return fade(startVolume, endVolume, duration, onFaded);
    }

    public Songs fade(float from, float to, float duration, Runnable onFaded) {
        Timer.newTimer(timerDuration -> {
            float percent = timerDuration / duration;
            if (percent > 1f)
                percent = 1f;
            if (percent < 0f)
                percent = 0f;

            float volume = (from * (1 - percent)) + (to * percent);
            setVolume(volume);

            if (volume == to) {
                if (onFaded != null)
                    onFaded.run();

                return false;
            }
            return true;
        }, 16L);
        setVolume(from);
        play();

        return this;
    }

    public Songs createHandle() {
        if (handle != null)
            handle.dispose(); //Safe checking

        handle = Gdx.audio.newMusic(Gdx.files.internal(path));
        handle.setVolume(GlobalOptions.musicVolume());
        disposed = false;
        return this;
    }

    private void dispose() {
        if (handle != null) {
            handle.dispose();
        }
        disposed = true;
    }

    public boolean isDisposed() {
        return disposed || handle == null;
    }
}
