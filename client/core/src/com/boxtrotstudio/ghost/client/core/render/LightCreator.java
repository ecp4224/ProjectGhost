package com.boxtrotstudio.ghost.client.core.render;


import box2dLight.p3d.P3dLight;
import org.jetbrains.annotations.NotNull;

public interface LightCreator {
    @NotNull
    P3dLight createLight();
}
