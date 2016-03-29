package me.eddiep.ghost.client.network;

import me.eddiep.ghost.client.utils.Global;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.util.zip.GZIPOutputStream;

/**
 * This class builds a Packet for a specified {@link me.eddiep.ghost.client.network.Client}
 * @param <C> The type of {@link me.eddiep.ghost.client.network.Client} this packet is meant for
 */
public class Packet<C extends Client> {

    private byte[] udpData;
    private ByteArrayOutputStream tempWriter;
    protected C client;
    private boolean ended;
    private int pos = 0;

    private boolean preserve;
    private byte[] preservedData;

    public Packet() { }

    public Packet attachPacket(byte[] data) {
        this.udpData = data;
        return this;
    }

    public void reuseFor(C client) {
        this.client = client;
        ended = false;
        pos = 0;
    }

    protected int getPosition() {
        return pos;
    }

    protected void setPosition(int pos) {
        this.pos = pos;
    }

    protected boolean isEnded() {
        return ended;
    }

    /**
     * Complete this packet and send it over TCP. This will execute {@link me.eddiep.ghost.client.network.Client#write(byte[])} with
     * the resulting byte array
      */
    public void endTCP() {
        if (client == null)
            return;

        byte[] data = endBytes();
        try {
            client.write(data);
        } catch (SocketException e) {
            if (!e.getMessage().contains("Connection reset")) {
                e.printStackTrace();
            } else {
                try {
                    client.disconnect();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void endTCPFlush() {
        if (client == null)
            return;

        byte[] data = endBytes();
        try {
            client.write(data);
            client.flush();
        } catch (SocketException e) {
            if (!e.getMessage().contains("Connection reset")) {
                e.printStackTrace();
            } else {
                try {
                    client.disconnect();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        client = null;
    }

    /**
     * Complete this packet and return a {@link DatagramPacket} which can be used to send over UDP
     * @return A {@link DatagramPacket} packet
     */
    public void endUdp() throws IOException {
        if (client == null)
            return;

        byte[] data = endBytes();
        client.writeUDP(data);
    }

    public byte[] endBytes() {
        if (preserve && preservedData != null)
            return preservedData;

        byte[] toReturn = new byte[0];
        if (tempWriter != null) {
            toReturn = tempWriter.toByteArray();
            try {
                tempWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (!preserve) {
            end();
        } else {
            preservedData = toReturn;
        }

        return toReturn;
    }

    private void end() {
        tempWriter = null;
        ended = true;
    }

    /**
     * Read a certain amount of data as a {@link me.eddiep.ghost.client.network.ConsumedData}. This can be used to
     * transform the read data into a Java primitive
     * @param length How much data to read
     * @return A {@link me.eddiep.ghost.client.network.ConsumedData} object to allow easy transformation of the data
     * @throws IOException If there was a problem reading the data
     * @see me.eddiep.ghost.client.network.ConsumedData
     */
    protected ConsumedData consume(int length) throws IOException {
        if (ended)
            throw new IOException("This packet has already ended!");

        if (udpData == null) {
            byte[] data = new byte[length];
            int endPos = pos + length;
            int i = 0;
            while (pos < endPos) {
                int r = client.read(data, i, length - i);
                if (r == -1)
                    throw new ArrayIndexOutOfBoundsException("Ran out of data to consume! (Consumed " + i + "/" + length + " bytes)");
                pos += r;
                i += r;
            }

            return new ConsumedData(data);
        } else {
            byte[] data = new byte[length];

            if (pos + length > this.udpData.length)
                throw new ArrayIndexOutOfBoundsException("Not enough data to consume! (Expected: " + length + " bytes, only " + (this.udpData.length - pos - 1) + " bytes left)");

            System.arraycopy(this.udpData, pos, data, 0, length);
            pos += length;

            return new ConsumedData(data);
        }
    }

    /**
     * Read a single byte or the entire packet as a {@link me.eddiep.ghost.client.network.ConsumedData}. This can be used to
     * transform the read data into a Java primitive. Whether this method reads a single byte or the entire packet depends on
     * whether this packet is reading from a {@link java.io.InputStream} or a byte array. If from a {@link java.io.InputStream}, then
     * it will return a single byte, otherwise the entire packet
     * @return A {@link me.eddiep.ghost.client.network.ConsumedData} object to allow easy transformation of the data
     * @throws IOException If there was a problem reading the data
     * @see me.eddiep.ghost.client.network.ConsumedData
     */
    protected ConsumedData consume() throws IOException {
        if (ended)
            throw new IOException("This packet has already ended!");

        if (udpData == null) {
            byte[] data = new byte[1];
            int r = client.read(data, 0, 1);
            pos += data.length;
            return new ConsumedData(data);
        } else {
            int toRead = udpData.length - pos;
            return consume(toRead);
        }
    }

    public Packet write(Object obj) throws IOException {
        if (preserve && preservedData != null)
            return this;

        String json = Global.GSON.toJson(obj);
        byte[] toWrite = json.getBytes(Charset.forName("ASCII"));

        if (toWrite.length > 600) { //Only ever gzip the json if it's bigger than 0.6kb
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            GZIPOutputStream gzip = new GZIPOutputStream(stream);
            gzip.write(toWrite);
            gzip.close();
            stream.close();

            byte[] data = stream.toByteArray();

            //4 + to include size of uncompressed
            write(4 + data.length); //The size of this chunk
            write(toWrite.length); //Size of uncompressed json
            write(data); //compressed json
        } else {
            write(4 + toWrite.length); //The size of this chunk
            write(toWrite.length); //Size of json string
            write(toWrite); //json
        }
        return this;
    }

    public Packet write(byte[] val) throws IOException {
        if (preserve && preservedData != null)
            return this;

        validateTempStream();
        tempWriter.write(val);
        return this;
    }

    public Packet write(byte[] val, int offset, int length) throws IOException {
        if (preserve && preservedData != null)
            return this;

        validateTempStream();
        tempWriter.write(val, offset, length);
        return this;
    }

    /**
     * Write a byte into this packet
     * @param val The byte value
     * @return This packet
     */
    public Packet write(byte val) {
        if (preserve && preservedData != null)
            return this;

        validateTempStream();
        tempWriter.write(val);
        return this;
    }

    /**
     * Write an int into this packet
     * @param val The int value
     * @throws IOException if there was a problem writing the value
     * @return This packet
     */
    public Packet write(int val) throws IOException {
        if (preserve && preservedData != null)
            return this;

        validateTempStream();
        tempWriter.write(ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(val).array());
        return this;
    }

    /**
     * Write a float into this packet
     * @param val The float value
     * @throws IOException if there was a problem writing the value
     * @return This packet
     */
    public Packet write(float val) throws IOException {
        if (preserve && preservedData != null)
            return this;

        validateTempStream();
        tempWriter.write(ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putFloat(val).array());
        return this;
    }

    /**
     * Write a double into this packet
     * @param val The double value
     * @throws IOException if there was a problem writing the value
     * @return This packet
     */
    public Packet write(double val) throws IOException {
        if (preserve && preservedData != null)
            return this;

        validateTempStream();
        tempWriter.write(ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN).putDouble(val).array());
        return this;
    }

    /**
     * Write a long into this packet
     * @param val The long value
     * @throws IOException if there was a problem writing the value
     * @return This packet
     */
    public Packet write(long val) throws IOException {
        if (preserve && preservedData != null)
            return this;

        validateTempStream();
        tempWriter.write(ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN).putLong(val).array());
        return this;
    }

    /**
     * Write a short into this packet
     * @param val The short value
     * @throws IOException if there was a problem writing the value
     * @return This packet
     */
    public Packet write(short val) throws IOException {
        if (preserve && preservedData != null)
            return this;

        validateTempStream();
        tempWriter.write(ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN).putShort(val).array());
        return this;
    }

    /**
     * Write a String into this packet, encoded as ASCII
     * @param string The String value
     * @throws IOException if there was a problem writing the value
     * @return This packet
     */
    public Packet write(String string) throws IOException {
        if (preserve && preservedData != null)
            return this;

        validateTempStream();
        tempWriter.write(string.getBytes(Charset.forName("ASCII")));
        return this;
    }

    /**
     * Write a String into this packet encoded as a given {@link Charset}
     * @param string The String value
     * @param charset The charset to encode the String as
     * @throws IOException if there was a problem writing the value
     * @return This packet
     */
    public Packet write(String string, Charset charset) throws IOException {
        if (preserve && preservedData != null)
            return this;

        validateTempStream();
        tempWriter.write(string.getBytes(charset));
        return this;
    }

    /**
     * Write a boolean into this packet. This will write 1 byte, 1 being true and 0 being false
     * @param value The boolean value
     * @throws IOException if there was a problem writing the value
     * @return This packet
     */
    public Packet write(boolean value) throws IOException {
        if (preserve && preservedData != null)
            return this;

        validateTempStream();
        tempWriter.write(value ? (byte)1 : (byte)0);
        return this;
    }

    /**
     * Append the current size of this packet to the front of the packet. This is useful for dynamic length packets
     * @return This packet
     * @throws IOException If there was an error creating the packet
     */
    public Packet appendSizeToFront() throws IOException {
        if (preserve && preservedData != null)
            return this;

        if (tempWriter == null)
            throw new IllegalStateException("No data written!");

        byte[] currentData = tempWriter.toByteArray();
        tempWriter.close();
        tempWriter = null; //Reset writer

        write(currentData[0]); //Write opCode first
        //We add 4 to include the space for this new info (the packet size)
        write(currentData.length + 4); //Then append size of packet to front of packet
        write(currentData, 1, currentData.length - 1); //Then write rest of packet
        return this;
    }

    private void validateTempStream() {
        if (tempWriter == null)
            tempWriter = new ByteArrayOutputStream();
    }

    /**
     * Read the contents of this packet and perform logic
     * @return This packet
     * @throws IOException If there was a problem reading the packet
     */
    public final Packet handlePacket(C client) throws IOException {
        this.client = client;
        handle();
        return this;
    }

    /**
     * Start writing this packet with the given data
     * @param args The data for this packet
     * @return This packet
     * @throws IOException If there was a problem reading the packet
     */
    public final Packet writePacket(C client, Object... args) throws IOException {
        this.client = client;

        preserve = false;
        preservedData = null;
        write(args);
        return this;
    }

    public final Packet writeAndPreservePacket(Object... args) throws IOException {
        preserve = true;
        write(args);
        return this;
    }

    protected void handle() throws IOException {
        throw new IllegalAccessError("This packet does not handle data!");
    }

    protected void write(Object... args) throws IOException {
        throw new IllegalAccessError("This packet does not write data!");
    }
}
