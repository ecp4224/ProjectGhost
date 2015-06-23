package me.eddiep.ghost.network.packet;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;

/**
 * Represented {@link me.eddiep.ghost.network.packet.ConsumedData} that can be transformed into a Java primative
 */
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

    /**
     * Transform this data to an int
     * @return The int value
     */
    public int asInt() {
        return buffer.getInt();
    }

    /**
     * Transform this data into a long
     * @return The long value
     */
    public long asLong() {
        return buffer.getLong();
    }

    /**
     * Transform this data into a float
     * @return The float value
     */
    public float asFloat() {
        return buffer.getFloat();
    }

    /**
     * Transform this data into a double
     * @return The double value
     */
    public double asDouble() {
        return buffer.getDouble();
    }

    /**
     * Transform this data into a short
     * @return The short value
     */
    public short asShort() {
        return buffer.getShort();
    }

    /**
     * Transform this data into a boolean. This read a single byte and returns true if the value is 1, or false if the value is 0
     * @return The boolean value
     */
    public boolean asBoolean() {
        return buffer.get() == 1;
    }

    /**
     * Transform this data into a String, decoded using ASCII
     * @return The String value
     */
    public String asString() {
        return new String(data);
    }

    /**
     * Transform this data into a String, decoded using the provided {@link java.nio.charset.Charset}
     * @param charset The charset to use for decoding
     * @return The Stirng value
     */
    public String asString(Charset charset) {
        return new String(data, charset);
    }

    /**
     * Transform this data into a single byte
     * @return The byte value
     */
    public byte asByte() {
        return data[0];
    }
}
