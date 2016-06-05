package com.boxtrotstudio.ghost.client.utils;


import com.badlogic.gdx.utils.Array;
import me.eddiep.jconfig.JConfig;
import me.eddiep.jconfig.system.Config;
import me.eddiep.jconfig.system.annotations.DefaultValue;
import me.eddiep.jconfig.system.annotations.Getter;
import me.eddiep.jconfig.system.annotations.Setter;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Locale;

public class GlobalOptions {
    private static GlobalConfig option;
    private static final File gameLocation;
    private static final Array<String> resolutions = new Array<>();

    static {
        final String OS = System.getProperty("os.name").toLowerCase(Locale.ENGLISH);

        if (OS.contains("win")) {
            gameLocation = new File(System.getenv("AppData"), "ghost");
        } else if (OS.contains("mac")) {
            gameLocation = new File("~/Library/Application Support", "Ghost");
        } else {
            gameLocation = new File("~/.ghost");
        }

        resolutions.add("1024x576");
        resolutions.add("1152x648");
        resolutions.add("1280x720");
        resolutions.add("1366x768");
        resolutions.add("1600x900");
        resolutions.add("1920x1080");
        resolutions.add("2560x1440");
        resolutions.add("3840x2160");
    }

    @NotNull
    public static GlobalConfig getOptions() {
        if (option == null) {
            option = JConfig.newConfigObject(GlobalConfig.class);

            File config = new File(gameLocation, "settings.conf");

            if (!gameLocation.exists())
                gameLocation.mkdirs();

                option.save(config);
        }

        return option;
    }

    @NotNull
    public static File getGameLocation() {
        return gameLocation;
    }

    @NotNull
    public static Array<String> getResolutions() {
        return resolutions;
    }

    @NotNull
    public static File getConfigLocation() {
        return new File(gameLocation, "settings.conf");
    }

    public interface GlobalConfig extends Config {

        @Getter(property = "volume")
        @DefaultValue(value = "1")
        float masterVolume();

        @Setter(property = "volume")
        void setMasterVolume(float val);

        @Getter(property = "musicVolume")
        @DefaultValue(value = "0.7")
        float musicVolume();

        @Setter(property = "musicVolume")
        void setMusicVolume(float val);

        @Getter(property = "fxVolume")
        @DefaultValue(value = "0.5")
        float fxVolume();

        @Setter(property = "fxVolume")
        void setFXVolume(float val);

        @Getter(property = "resolution")
        @DefaultValue(value = "1280x720")
        String resolution();

        @Setter(property = "resolution")
        void setResolution(String resolution);

        @Getter(property = "fullscreen")
        @DefaultValue(value = "true")
        boolean fullscreen();

        @Setter(property = "fullscreen")
        void setFullscreen(boolean val);

        @Getter(property = "displayFPS")
        @DefaultValue(value = "false")
        boolean displayFPS();

        @Setter(property = "displayFPS")
        void setDisplayFPS(boolean val);

        @Getter(property = "displayPing")
        @DefaultValue(value = "false")
        boolean displayPing();

        @Setter(property = "displayPing")
        void setDisplayPing(boolean val);

        @Getter(property = "firstRun")
        @DefaultValue(value = "true")
        boolean isFirstRun();

        @Setter(property = "firstRun")
        void setFirstRun(boolean val);
    }
}
