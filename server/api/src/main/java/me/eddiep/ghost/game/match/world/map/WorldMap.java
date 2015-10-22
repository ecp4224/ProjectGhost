package me.eddiep.ghost.game.match.world.map;

import me.eddiep.ghost.utils.Global;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Scanner;

public class WorldMap {
    private String name;
    private String backgroundTexture;
    private EntityLocations[] locations;
    private float ambiantPower = 1f;
    private AmbiantColor ambiantColor = new AmbiantColor();

    public static WorldMap fromFile(File file) throws FileNotFoundException {
        if (!file.exists())
            return null;

        Scanner scanner = new Scanner(file);
        scanner.useDelimiter("\\Z");
        String content = scanner.next();
        scanner.close();

        return Global.GSON.fromJson(content, WorldMap.class);
    }

    private WorldMap() { }

    public String getName() {
        return name;
    }

    public String getBackgroundTexture() {
        return backgroundTexture;
    }

    public float getAmbiantPower() {
        return ambiantPower;
    }

    public int[] getAmbiantColor() {
        return new int[] {
                ambiantColor.red,
                ambiantColor.green,
                ambiantColor.blue
        };
    }

    public EntityLocations[] getStartingLocations() {
        return locations;
    }

    public void dispose() {
        locations = null;
        name = null;
        backgroundTexture = null;
    }

    public class EntityLocations {
        private short id;
        private float x;
        private float y;
        private short width;
        private short height;
        private double rotation;
        private HashMap<String, String> extras;

        public short getId() {
            return id;
        }

        public float getX() {
            return x;
        }

        public float getY() {
            return y;
        }

        public short getWidth() {
            return width;
        }

        public short getHeight() {
            return height;
        }

        public double getRotation() {
            return rotation;
        }

        public String getExtra(String key) {
            if (extras == null)
                return null;
            return extras.get(key);
        }
    }

    public class AmbiantColor {
        private int red = 255;
        private int green = 255;
        private int blue = 255;
    }
}
