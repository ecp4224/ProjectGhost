package me.eddiep.ghost.client.core.render.scene;

import com.badlogic.gdx.Gdx;
import me.eddiep.ghost.client.Ghost;

public abstract class AbstractScene implements Scene {
    private boolean visible = true;
    private String name = "AbstractScene";
    private int order = 0;

    protected int width, height;

    public AbstractScene() {
        width = Gdx.graphics.getWidth();
        height = Gdx.graphics.getHeight();
    }

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

    protected void replaceWith(Scene scene) {
        Ghost.getInstance().removeScene(this);
        Ghost.getInstance().addScene(scene);
    }
}
