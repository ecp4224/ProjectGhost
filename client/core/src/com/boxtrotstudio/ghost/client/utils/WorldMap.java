package com.boxtrotstudio.ghost.client.utils;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Scanner;

public class WorldMap {
    private String name;
    private String backgroundTexture;
    private EntityLocation[] locations;
    private float ambiantPower = 0.6f;
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

    public void setName(String name) {
        this.name = name;
    }

    public void setBackgroundTexture(String backgroundTexture) {
        this.backgroundTexture = backgroundTexture;
    }

    public void setLocations(EntityLocation[] locations) {
        this.locations = locations;
    }

    public void setAmbiantPower(float ambiantPower) {
        this.ambiantPower = ambiantPower;
    }

    public void setAmbiantColor(AmbiantColor ambiantColor) {
        this.ambiantColor = ambiantColor;
    }

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

        public void setId(short id) {
            this.id = id;
        }

        public void setX(float x) {
            this.x = x;
        }

        public void setY(float y) {
            this.y = y;
        }

        public void setWidth(short width) {
            this.width = width;
        }

        public void setHeight(short height) {
            this.height = height;
        }

        public void setRotation(double rotation) {
            this.rotation = rotation;
        }

        public void addExtra(String key, String value) {
            if (extras == null)
                extras = new HashMap<>();
            extras.put(key, value);
        }

        public String getExtra(String key) {
            if (extras == null)
                return null;
            return extras.get(key);
        }

        public void setColor(int[] color) {
            this.color = color;
        }

        public int[] getColor() {
            return color;
        }

        public boolean hasExtra(String radius) {
            return extras != null && extras.containsKey(radius);
        }
    }

    public class AmbiantColor {
        public int red = 0;
        public int green = 0;
        public int blue = 0;
    }
}

