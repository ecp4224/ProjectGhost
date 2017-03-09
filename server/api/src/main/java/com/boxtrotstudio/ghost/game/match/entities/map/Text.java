package com.boxtrotstudio.ghost.game.match.entities.map;

import com.boxtrotstudio.ghost.game.match.world.World;
import com.boxtrotstudio.ghost.utils.Vector2f;
import com.boxtrotstudio.ghost.utils.annotations.Bind;
import com.boxtrotstudio.ghost.utils.builder.Binder;
import com.boxtrotstudio.ghost.utils.builder.Builder;

import java.awt.*;

public class Text {
    public static final int SHADOW = (1 << 0);
    public static final int BOLD = (1 << 1);
    public static final int ITALIC = (1 << 2);
    public static final int TUTORIAL = (1 << 3);
    private static long nextID = 1L;

    private String text;
    private Vector2f position;
    private int size = 12;
    private Color color = Color.WHITE;
    private int textOptions;

    private long id;

    public static TextBuilder create() {
        return Binder.newBinderObject(TextBuilder.class);
    }

    Text(String text, Vector2f position, int size, Color color, int type, long id) {
        this.text = text;
        this.position = position;
        this.size = size;
        this.color = color;
        this.textOptions = type;
        this.id = id;
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

    public int getTextOptions() {
        return textOptions;
    }

    public long getID() {
        return id;
    }

    public void displayIn(World world) {
        world.displayText(this);
    }

    public void removeFrom(World world) {
        world.removeText(this);
    }

    public interface TextBuilder extends Builder<Text> {

        @Bind(properties = "type")
        TextBuilder options(int options);

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

        @Bind
        int getOptions();

        default Vector2f getPosition() {
            return new Vector2f(getX(), getY());
        }

        default Text build() {
            return new Text(getText(), getPosition(), getSize(), getColor(), getOptions(), nextID++);
        }
    }
}
