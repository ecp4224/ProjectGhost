package me.eddiep.ghost.server.utils;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class ArrayHelper {

    public static <T> void assertTrueFor(T[] collection, PFunction<T, Boolean> func, String failedMessage) {
        for (T temp : collection) {
            try {
                if (!func.run(temp)) {
                    throw new IllegalStateException(failedMessage);
                }
            } catch (Throwable t) {
                throw new IllegalStateException(failedMessage, t);
            }
        }
    }

    public static <T> void forEach(T[] collection, PRunnable<T> func) {
        for (T temp : collection) {
            func.run(temp);
        }
    }

    public static <T> T[] combind(T[] a, T[] b) {
        int aLen = a.length;
        int bLen = b.length;

        @SuppressWarnings("unchecked")
        T[] c = (T[]) Array.newInstance(a.getClass().getComponentType(), aLen + bLen);
        System.arraycopy(a, 0, c, 0, aLen);
        System.arraycopy(b, 0, c, aLen, bLen);

        return c;
    }
}
