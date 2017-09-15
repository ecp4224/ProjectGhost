package com.boxtrotstudio.ghost.client.utils;


import com.badlogic.gdx.utils.Array;
import com.boxtrotstudio.ghost.client.Ghost;
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
    private static final String OS;

    static {
        OS = System.getProperty("os.name").toLowerCase(Locale.ENGLISH);

        if (OS.contains("win")) {
            gameLocation = new File(System.getenv("AppData"), "ghost");
        } else {
            gameLocation = new File(System.getProperty("user.home"), ".ghost");
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

            if (!gameLocation.exists()) {
                boolean result = gameLocation.mkdirs();
                if (!result) {
                    return option; //Return default options if we can't save/load from this directory
                }
            }

            if (!config.exists()) {
                option.save(config);
            } else if (config.exists()) {
                option.load(config);
            }
        }

        return option;
    }

    public static float fxVolume() {
        return option.fxVolume() * option.masterVolume();
    }

    public static float musicVolume() {
        return option.musicVolume() * option.masterVolume();
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

    @NotNull
    public static String getOS() {
        return OS;
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
        @DefaultValue(value = "false")
        boolean fullscreen();

        @Setter(property = "fullscreen")
        void setFullscreen(boolean val);

        @Getter(property = "displayFPS")
        @DefaultValue(value = "false")
        boolean displayFPS();

        @Setter(property = "displayFPS")
        @DefaultValue(value = "true")
        void setDisplayFPS(boolean val);

        @Getter(property = "displayPing")
        @DefaultValue(value = "true")
        boolean displayPing();

        @Setter(property = "displayPing")
        void setDisplayPing(boolean val);

        @Getter(property = "firstRun")
        @DefaultValue(value = "true")
        boolean isFirstRun();

        @Setter(property = "firstRun")
        void setFirstRun(boolean val);

        @Getter(property = "invertMouseControls")
        @DefaultValue(value = "false")
        boolean isMouseInverted();

        @Getter(property = "usePathfinding")
        @DefaultValue(value = "false")
        boolean isPathfinding();

        @Setter(property = "invertMouseControls")
        void setMouseInverted(boolean val);

        @Setter(property = "usePathfinding")
        void setPathfinding(boolean val);

        @Getter(property = "lastWeapon")
        @DefaultValue(value = "1")
        int getLastWeapon();

        @Getter(property = "lastItem")
        @DefaultValue(value = "0")
        int getLastItem();

        @Setter(property = "lastWeapon")
        void setLastWeapon(int weapon);

        @Setter(property = "lastItem")
        void setLastItem(int item);
    }
}
