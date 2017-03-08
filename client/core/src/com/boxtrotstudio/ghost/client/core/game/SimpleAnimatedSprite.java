package com.boxtrotstudio.ghost.client.core.game;

import com.boxtrotstudio.ghost.client.core.game.animations.Animation;
import com.boxtrotstudio.ghost.client.core.game.animations.AnimationType;
import com.boxtrotstudio.ghost.client.utils.Direction;

public class SimpleAnimatedSprite extends SpriteEntity {
    public SimpleAnimatedSprite(String path, short id, int framecount, int speed) {
        super(path, id);

        int height = (int) getHeight();
        int width = (int)(getWidth() / framecount);

        Animation defaultAnimation = Animation.newBuilder()
                .setFramecount(framecount)
                .setWidth(width)
                .setHeight(height)
                .setSpeed(speed)
                .build();

        attachAnimations(defaultAnimation);
    }

    public Animation getAnimation() {
        return getAnimation(AnimationType.CUSTOM, Direction.DEFAULT);
    }
}
