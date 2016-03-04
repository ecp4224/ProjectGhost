package me.eddiep.ghost.utils;

import java.nio.ByteBuffer;

public class NetworkUtils {
    public static byte[] float2ByteArray (float value) {
        return ByteBuffer.allocate(4).putFloat(value).array();
    }

    public static byte[] int2ByteArray (int value) {
        return ByteBuffer.allocate(4).putInt(value).array();
    }

    public static int byteArray2Int(byte[] array) {
        return ByteBuffer.wrap(array).getInt();
    }

    public static double storeFloats(float a, float b) {
        byte[] aBytes = float2ByteArray(a);
        byte[] bBytes = float2ByteArray(b);

        ByteBuffer buffer = ByteBuffer.allocate(8).put(aBytes).put(bBytes);
        buffer.rewind();
        return buffer.getDouble();
    }

    public static double storeInts(int a, int b) {
        byte[] aBytes = int2ByteArray(a);
        byte[] bBytes = int2ByteArray(b);

        ByteBuffer buffer = ByteBuffer.allocate(8).put(aBytes).put(bBytes);
        buffer.rewind();
        return buffer.getDouble();
    }
}
