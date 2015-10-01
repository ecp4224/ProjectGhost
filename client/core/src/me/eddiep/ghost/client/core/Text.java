package me.eddiep.ghost.client.core;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;

import java.util.ArrayList;

public class Text implements Drawable, Attachable {
    private static final GlyphLayout layout = new GlyphLayout();

    private final FileHandle handle;
    private final int size;
    private final Color color;

    private BitmapFont font;
    private float x, y;
    private String text;

    private ArrayList<Attachable> children = new ArrayList<Attachable>();
    private ArrayList<Attachable> parents = new ArrayList<Attachable>();


    public Text(int size, Color color, FileHandle file) {
        this.size = size;
        this.color = color;
        handle = file;
    }

    @Override
    public void draw(SpriteBatch batch) {
        font.draw(batch, text, x, y);
    }

    @Override
    public void load() {
        FreeTypeFontGenerator gen = new FreeTypeFontGenerator(handle);
        FreeTypeFontGenerator.FreeTypeFontParameter parm = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parm.size = size;
        parm.color = color;

        font = gen.generateFont(parm);
        gen.dispose();
    }

    @Override
    public void unload() {
        font.dispose();
        text = null;
        font = null;
    }

    @Override
    public void attach(Attachable attach) {
        children.add(attach);
        attach.addParent(this);
    }

    @Override
    public void deattach(Attachable attach) {
        children.remove(attach);
        attach.removeParent(this);
    }

    @Override
    public float getX() {
        return x;
    }

    @Override
    public void setX(float x) {
        this.x = x;

        for (Attachable c : children) {
            c.setX(x);
        }
    }

    @Override
    public float getY() {
        return y;
    }

    @Override
    public void setY(float y) {
        this.y = y;

        for (Attachable c : children) {
            c.setY(y);
        }
    }

    @Override
    public void addParent(Attachable parent) {
        parents.remove(parent);
    }

    @Override
    public void removeParent(Attachable parent) {
        parents.remove(parent);
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

}
