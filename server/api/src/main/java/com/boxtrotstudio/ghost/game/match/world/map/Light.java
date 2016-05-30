package com.boxtrotstudio.ghost.game.match.world.map;

import com.boxtrotstudio.ghost.utils.Vector2f;

import java.awt.*;

public class Light {
    private float x, y;
    private float radius, intensity;
    private Color color;

    public Light() {
        this(0f, 0f, 50, 10, Color.WHITE);
    }

    public Light(float x, float y) {
        this(x, y, 50f, 10f, Color.WHITE);
    }

    public Light(float x, float y, float radius) {
        this(x, y, radius, 10, Color.WHITE);
    }

    public Light(float x, float y, float radius, float intensity) {
        this(x, y, radius, intensity, Color.WHITE);
    }

    public Light(float x, float y, float radius, float intensity, Color color) {
        this.x = x;
        this.y = y;
        this.radius = radius;
        this.intensity = intensity;
        this.color = color;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getRadius() {
        return radius;
    }

    public float getIntensity() {
        return intensity;
    }

    public Color getColor() {
        return color;
    }

    public int getColor888() {
        return (color.getRed() << 24) |
                (color.getGreen() << 16) |
                (color.getBlue() << 8) |
                color.getAlpha();
    }

    public Vector2f getVector() {
        return new Vector2f(x, y);
    }

    public void setPosition(Vector2f vector2f) {
        this.x = vector2f.x;
        this.y = vector2f.y;
    }

    public void setX(float x) {
        this.x = x;
    }

    public void setY(float y) {
        this.y = y;
    }

    public void setRadius(float radius) {
        this.radius = radius;
    }

    public void setIntensity(float intensity) {
        this.intensity = intensity;
    }

    public void setColor(Color color) {
        this.color = color;
    }
}
