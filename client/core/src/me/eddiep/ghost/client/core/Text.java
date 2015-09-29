package me.eddiep.ghost.client.core;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;

public class Text implements Drawable {
    private final FileHandle handle;
    private final int size;

    private BitmapFont font;
    private float x, y;
    private String text;

    public Text(int size, FileHandle file) {
        this.size = size;
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

        font = gen.generateFont(parm);
        gen.dispose();
    }

    @Override
    public void unload() {
        font.dispose();
        text = null;
        font = null;
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

}
