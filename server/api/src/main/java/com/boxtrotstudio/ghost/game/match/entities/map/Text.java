package com.boxtrotstudio.ghost.game.match.entities.map;

import com.boxtrotstudio.ghost.game.match.world.World;
import com.boxtrotstudio.ghost.utils.Vector2f;
import com.boxtrotstudio.ghost.game.match.entities.BaseEntity;
import com.boxtrotstudio.ghost.game.match.entities.TypeableEntity;
import com.boxtrotstudio.ghost.utils.NetworkUtils;

import java.awt.*;

public class Text extends BaseEntity implements TypeableEntity {

    private int size = 12;
    private Color color = Color.WHITE;
    public static Text display(String text) {
        return new Text(text);
    }

    private Text(String text) {
        super();
        setName(text); //Store text in name property
    }

    public Text at(float x, float y) {
        setPosition(new Vector2f(x, y));
        return this;
    }

    public Text at(Vector2f pos) {
        setPosition(pos);
        return this;
    }

    public Text withSize(int size) {
        this.size = size;
        return this;
    }

    public Text withColor(Color color) {
        this.color = color;
        return this;
    }

    public Color getColor() {
        return color;
    }

    public int getSize() {
        return size;
    }

    public Text in(World world) {
        int color888 = (color.getRed() << 24) |
                (color.getGreen() << 16) |
                (color.getBlue() << 8) |
                color.getAlpha();

        double rotationValue = NetworkUtils.storeInts(size, color888);
        super.setRotation(rotationValue);
        world.spawnEntity(this);

        return this;
    }

    @Override
    public void setRotation(double value) {
        throw new UnsupportedOperationException("The Text Entity does not support rotation!");
    }

    @Override
    public short getType() {
        return 91;
    }
}
