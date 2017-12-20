package com.boxtrotstudio.ghost.client.core.game.animations;

import com.boxtrotstudio.ghost.client.utils.Direction;
import com.boxtrotstudio.ghost.client.utils.annotations.Bind;
import com.boxtrotstudio.ghost.client.utils.builder.Builder;

import java.util.ArrayList;
import java.util.List;

public interface AnimationBuilder extends Builder<Animation> {

    @Bind(properties = "type")
    AnimationBuilder setType(AnimationType type);

    @Bind(properties = "direction")
    AnimationBuilder setDirection(Direction direction);

    @Bind(properties = "reverse")
    AnimationBuilder setReverse(boolean reverse);

    @Bind(properties = "sequence")
    AnimationBuilder setSequence(int[] sequence);

    @Bind(properties = "variants")
    AnimationBuilder setVariants(List<AnimationVariant> variants);

    @Bind
    AnimationType getType();

    @Bind
    Direction getDirection();

    @Bind(properties = "x")
    AnimationBuilder setX(int x);

    @Bind(properties = "y")
    AnimationBuilder setY(int y);

    @Bind(properties = "width")
    AnimationBuilder setWidth(int width);

    @Bind(properties = "height")
    AnimationBuilder setHeight(int height);

    @Bind(properties = "framecount")
    AnimationBuilder setFramecount(int framecount);

    @Bind(properties = "speed")
    AnimationBuilder setSpeed(int speed);

    @Bind
    int getX();

    @Bind
    int getY();

    @Bind
    int getWidth();

    @Bind
    int getHeight();

    @Bind
    int getFramecount();

    @Bind
    int getSpeed();

    @Bind
    List<AnimationVariant> getVariants();

    @Bind
    boolean getReverse();

    @Bind
    int[] getSequence();

    default AnimationType getDefaultType() {
        return getType() == null ? AnimationType.CUSTOM : getType();
    }

    default int[] getDefaultSequence() {
        return getSequence() == null ? new int[0] : getSequence();
    }

    default List<AnimationVariant> getDefaultVariants() {
        return getVariants() == null ? new ArrayList<>() : getVariants();
    }

    default Direction getDefaultDirection() {
        return getDirection() == null ? Direction.DEFAULT : getDirection();
    }

    @Override
    default Animation build() {
        return new Animation(
                getDefaultType(), getDefaultDirection(), getX(),
                getY(), getWidth(), getHeight(), getFramecount(),
                getSpeed(),  getReverse(), getDefaultSequence(), getDefaultVariants()
        );
    }
}
