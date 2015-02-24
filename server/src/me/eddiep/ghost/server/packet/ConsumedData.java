package me.eddiep.ghost.server.packet;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

public class ConsumedData {
    private byte[] data;
    private ByteBuffer buffer;

    ConsumedData(byte[] data) {
        this.data = data;
        buffer = ByteBuffer.allocate(data.length);
        buffer.put(data);
    }

    public int asInt() {
        return buffer.getInt();
    }

    public long asLong() {
        return buffer.getLong();
    }

    public float asFloat() {
        return buffer.getFloat();
    }

    public double asDouble() {
        return buffer.getDouble();
    }

    public short asShort() {
        return buffer.getShort();
    }

    public boolean asBoolean() {
        return buffer.get() == 1;
    }

    public String asString() {
        return new String(data);
    }

    public String asString(Charset charset) {
        return new String(data, charset);
    }
}
