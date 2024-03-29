package com.boxtrotstudio.ghost.game.match.world.map;

import com.boxtrotstudio.ghost.utils.Global;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Scanner;

public class WorldMap {
    private String name;
    private String backgroundTexture;
    private EntityLocation[] locations;
    private float ambientPower = 0.6f;
    private AmbientColor ambientColor = new AmbientColor();

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

    public float getAmbientPower() {
        return ambientPower;
    }

    public int[] getAmbientColor() {
        return new int[] {
                ambientColor.red,
                ambientColor.green,
                ambientColor.blue
        };
    }

    public EntityLocation[] getStartingLocations() {
        return locations;
    }

    public void dispose() {
        locations = null;
        name = null;
        backgroundTexture = null;
    }

    public class EntityLocation {
        private short id;
        private float x;
        private float y;
        private short width;
        private short height;
        private double rotation;
        private HashMap<String, String> extras;
        private int[] color = new int[] { 255, 255, 255 }; //Default white

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

        public int[] getColor() {
            return color;
        }

        public boolean hasExtra(String radius) {
            return extras != null && extras.containsKey(radius);
        }
    }

    public class AmbientColor {
        private int red;
        private int green;
        private int blue;
    }
}
