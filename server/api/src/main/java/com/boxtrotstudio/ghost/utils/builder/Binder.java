package com.boxtrotstudio.ghost.utils.builder;

import java.lang.reflect.Modifier;

public class Binder {

    public static <T extends Bindable> T newBinderObject(Class<T> configClass) {
        if (!Modifier.isInterface(configClass.getModifiers())) {
            throw new IllegalArgumentException("The class must be an interface!");
        }

        return BinderProxy.createBindableProxyx(configClass);
    }
}
