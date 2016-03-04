package me.eddiep.ghost.client.core.render;


import box2dLight.Light;
import org.jetbrains.annotations.NotNull;

public interface LightCreator {
    @NotNull Light createLight();
}
