package me.eddiep.ghost.utils;

import java.nio.ByteBuffer;

public class NetworkUtils {
    public static byte[] float2ByteArray (float value) {
        return ByteBuffer.allocate(4).putFloat(value).array();
    }

    public static int byteArray2Int(byte[] array) {
        return ByteBuffer.allocate(array.length).getInt();
    }
}
