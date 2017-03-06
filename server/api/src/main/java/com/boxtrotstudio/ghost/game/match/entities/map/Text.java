package com.boxtrotstudio.ghost.game.match.entities.map;

import com.boxtrotstudio.ghost.utils.Vector2f;
import com.boxtrotstudio.ghost.utils.annotations.Bind;
import com.boxtrotstudio.ghost.utils.builder.Binder;
import com.boxtrotstudio.ghost.utils.builder.Builder;

import java.awt.*;

public class Text {
    private String text;
    private Vector2f position;
    private int size = 12;
    private Color color = Color.WHITE;

    public static TextBuilder create() {
        return Binder.newBinderObject(TextBuilder.class);
    }

    public Text(String text, Vector2f position, int size, Color color) {
        this.text = text;
        this.position = position;
        this.size = size;
        this.color = color;
    }

    public String getText() {
        return text;
    }

    public Vector2f getPosition() {
        return position;
    }

    public int getSize() {
        return size;
    }

    public Color getColor() {
        return color;
    }

    public interface TextBuilder extends Builder<Text> {

        @Bind(properties = {"x", "y"})
        TextBuilder position(float x, float y); //This will save the two parameters as properties {x, y}

        @Bind(properties = {"text"})
        TextBuilder text(String text); //This will save the parameter as the property {text}

        @Bind(properties = {"color"})
        TextBuilder color(Color color); //This will save the parameter as the property {color}

        @Bind(properties = {"size"})
        TextBuilder size(int size); //This will save the parameter as the property {size}

        @Bind
        float getX(); //This will return the property x

        @Bind
        float getY(); //This will return the property y

        @Bind
        int getSize(); //This will return the property size

        @Bind
        Color getColor(); //This will return the property color

        @Bind
        String getText(); //This will return the property text

        default Vector2f getPosition() {
            return new Vector2f(getX(), getY());
        }

        default Text build() {
            return new Text(getText(), getPosition(), getSize(), getColor());
        }
    }
}
