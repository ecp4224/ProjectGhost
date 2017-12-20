package com.boxtrotstudio.ghost.utils.builder;

import com.boxtrotstudio.ghost.utils.PrimitiveDefaults;
import com.boxtrotstudio.ghost.utils.annotations.Bind;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class BinderProxy implements InvocationHandler {
    private HashMap<String, Object> values = new HashMap<>();
    private HashMap<Method, Bind> methods = new HashMap<>();
    private Class<? extends Bindable> binderClass;
    private Bindable original;

    private MethodHandles.Lookup privateLookup;

    private BinderProxy(Class<? extends Bindable> binderClass) {
        setupBind(binderClass);
        this.binderClass = binderClass;

        try {
            Constructor<MethodHandles.Lookup> constructorLookup = MethodHandles.Lookup.class.getDeclaredConstructor(Class.class, int.class);
            if (!constructorLookup.isAccessible()) {
                constructorLookup.setAccessible(true);
            }
            privateLookup = constructorLookup.newInstance(binderClass, MethodHandles.Lookup.PRIVATE);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException e) {
            e.printStackTrace();
        }
    }

    private void setupBind(Class<? extends Bindable> configClass) {
        List<Method> methods = new ArrayList<>(Arrays.asList(configClass.getMethods()));

        Class parent = configClass.getSuperclass();
        while (parent != null) {
            methods.addAll(Arrays.asList(parent.getDeclaredMethods()));
            parent = configClass.getSuperclass();
        }

        for (Method m : methods) {
            Annotation[] prop = m.getDeclaredAnnotations();
            for (Annotation annotation : prop) {
                if (annotation instanceof Bind) {
                    this.methods.put(m, (Bind)annotation);
                }
            }
        }


    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Class<?> returnType = method.getReturnType();
        if (methods.containsKey(method)) {
            Bind b = methods.get(method);

            //First save values if any
            Parameter[] parameters = method.getParameters();
            for (int i = 0; i < parameters.length; i++) {
                Object value = args[i];

                String name = b.properties()[i];

                values.put(name, value);
            }

            //Now see if what we need to return
            if (!returnType.equals(Void.TYPE)) {
                if (returnType.equals(binderClass)) {
                    return proxy;
                } else if (!b.returnProperty().equals("")) {
                    return values.get(b.returnProperty());
                } else if (method.getName().startsWith("get")) {
                    String name = method.getName().substring(3).toLowerCase();

                    if (!values.containsKey(name)) {
                        if (returnType.isPrimitive()) {
                            Object value;
                            if (!b.defaultValue().equals("")) {
                                value = convertRawValue(b.defaultValue(), returnType);
                            } else {
                                value = PrimitiveDefaults.getDefaultValue(returnType);
                            }

                            values.put(name, value);
                        }
                    }

                    return values.get(name);
                } else {
                    //Return the first argument that matches the return type
                    for (Object arg : args) {
                        if (returnType.equals(arg.getClass()))
                            return arg;
                    }

                    System.err.println("Return type invalid! Returning null!");
                    return null;
                }
            }
        }

        if (method.isDefault() && privateLookup != null) {
            Class<?> declaringClass = method.getDeclaringClass();

            return privateLookup
                    .in(declaringClass)
                    .unreflectSpecial(method, declaringClass)
                    .bindTo(proxy)
                    .invokeWithArguments(args);
        }

        return null;
    }

    private Object convertRawValue(String rawValue, Class<?> returnType) {
        if (returnType.equals(String.class))
            return rawValue;
        if (returnType.equals(int.class)) {
            return Integer.parseInt(rawValue);
        }
        if (returnType.equals(boolean.class)) {
            return Boolean.parseBoolean(rawValue);
        }
        if (returnType.equals(float.class)) {
            return Float.parseFloat(rawValue);
        }
        if (returnType.equals(double.class)) {
            return Double.parseDouble(rawValue);
        }
        if (returnType.equals(long.class)) {
            return Long.parseLong(rawValue);
        }
        if (returnType.equals(byte.class)) {
            return Byte.parseByte(rawValue);
        }
        if (returnType.equals(short.class)) {
            return Short.parseShort(rawValue);
        }
        if (returnType.equals(char.class)) {
            return rawValue.toCharArray()[0];
        }

        return null;
    }

    public static <T extends Bindable> T createBindableProxyx(Class<T> configClass) {
        BinderProxy proxy = new BinderProxy(configClass);

        T obj = (T) Proxy.newProxyInstance(configClass.getClassLoader(), new Class[] { configClass}, proxy);
        proxy.original = obj;

        return obj;
    }
}
