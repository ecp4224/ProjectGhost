package com.boxtrotstudio.ghost.client.core.render.scene;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.boxtrotstudio.ghost.client.Ghost;
public abstract class AbstractScene implements Scene {
    private boolean visible = true;
    private String name = "AbstractScene";
    private Stage stage;
    private int order;

    protected int width, height;

    protected boolean wasInit;

    public AbstractScene() {
        width = (int) Ghost.getInstance().viewport.getWorldWidth();
        height = (int) Ghost.getInstance().viewport.getWorldHeight();
        /*width = Gdx.graphics.getWidth();
        height = Gdx.graphics.getHeight();*/
    }

    @Override
    public void init() {
        if (wasInit) {
            System.out.println("Already loaded " + getClass().getSimpleName());
            return;
        }
        onInit();

        if (stage != null) {
            Gdx.input.setInputProcessor(stage);
        }

        wasInit = true;
    }

    protected Drawable grabDrawable(String path) {
        Texture txt = Ghost.ASSETS.get(path, Texture.class);
        txt.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        return new TextureRegionDrawable(new TextureRegion(txt));
    }

    protected final void attachStage(Stage stage) {
        this.stage = stage;
    }

    protected void onInit() { }

    @Override
    public boolean isVisible() {
        return visible;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
        if (stage != null) {
            if (!visible && Gdx.input.getInputProcessor().equals(stage)) {
                Gdx.input.setInputProcessor(null);
            }
        }
    }

    @Override
    public int requestedOrder() {
        return order;
    }

    public void requestOrder(int order) {
        this.order = order;
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public void replaceWith(Scene scene) {
        Ghost.getInstance().removeScene(this);
        Ghost.getInstance().addScene(scene);
    }

    @Override
    public void softReplace(Scene scene) {
        scene.setVisible(true);
        setVisible(false);
    }
}
