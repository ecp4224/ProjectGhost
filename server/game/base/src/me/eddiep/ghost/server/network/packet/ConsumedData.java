package me.eddiep.ghost.server.network.packet;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;

public class ConsumedData {
    private byte[] data;
    private ByteBuffer buffer;

    ConsumedData(byte[] data, boolean flip) {
        this.data = data;
        buffer = ByteBuffer.allocate(data.length);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.put(data);
        buffer.position(0);
        if (flip)
            buffer.flip();
    }

    ConsumedData(byte[] data) {
        this(data, false);
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
        return new String(data, Charset.forName("ASCII"));
    }

    public String asString(Charset charset) {
        return new String(data, charset);
    }

    public byte asByte() {
        return data[0];
    }
}
