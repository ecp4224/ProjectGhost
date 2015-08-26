package me.eddiep.ghost.network.packet;

import me.eddiep.ghost.network.Client;
import me.eddiep.ghost.network.Server;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.util.zip.GZIPOutputStream;

import static me.eddiep.ghost.utils.Global.GSON;

/**
 * This class builds a Packet for a specified {@link me.eddiep.ghost.network.Server} and a specified {@link me.eddiep.ghost.network.Client}
 * @param <T> The type of {@link me.eddiep.ghost.network.Server} this packet is meant for
 * @param <C> The type of {@link me.eddiep.ghost.network.Client} this packet is meant for
 */
public class Packet<T extends Server, C extends Client<T>> {

    private byte[] udpData;
    private ByteArrayOutputStream tempWriter;
    private C client;
    private boolean ended;
    private int pos = 0;

    /**
     * Create a new packet processor that reads data dynamically. Use this constructor for TCP Packets
     * @param client The client this packet came from
     */
    public Packet(C client) {
        this.client = client;
    }

    /**
     * Create a new packet processor that reads data that is provided. Use this constructor for UDP Packets
     * @param client The client this packet came from
     * @param data The packet to process
     */
    public Packet(C client, byte[] data) {
        this.client = client;
        this.udpData = data;
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
     * Complete this packet and send it over TCP. This will execute {@link me.eddiep.ghost.network.Client#write(byte[])} with
     * the resulting byte array
      */
    public void endTCP() {
        if (client == null)
            return;

        if (tempWriter != null) {
            try {

                if (client.getServer().isDebugMode()) {
                    System.err.println("[DEBUG] Sending TCP packet " + getClass().getCanonicalName());
                }

                byte[] data = tempWriter.toByteArray();
                client.write(data);
                tempWriter.close();
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
        end();
    }

    public void endTCPFlush() {
        if (client == null)
            return;

        if (tempWriter != null) {
            try {

                if (client.getServer().isDebugMode()) {
                    System.err.println("[DEBUG] Sending TCP packet " + getClass().getCanonicalName());
                }

                byte[] data = tempWriter.toByteArray();
                client.write(data);
                client.flush();
                tempWriter.close();
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
        end();
    }

    /**
     * Complete this packet and return a {@link java.net.DatagramPacket} which can be used to send over UDP
     * @return A {@link java.net.DatagramPacket} packet
     */
    public DatagramPacket endUDP() {
        if (client == null)
            return null;

        DatagramPacket packet = null;
        if (tempWriter != null) {
            try {
                if (client.getServer().isDebugMode()) {
                    System.err.println("[DEBUG] Creating UDP packet " + getClass().getCanonicalName());
                }

                byte[] data = tempWriter.toByteArray();
                packet = new DatagramPacket(data, 0, data.length, client.getIpAddress(), client.getPort());
                tempWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        end();
        return packet;
    }

    public byte[] endBytes() {
        byte[] toReturn = new byte[0];
        if (tempWriter != null) {
            toReturn = tempWriter.toByteArray();
        }
        end();
        return toReturn;
    }

    private void end() {
        tempWriter = null;
        client = null;
        ended = true;
    }

    /**
     * Read a certain amount of data as a {@link me.eddiep.ghost.network.packet.ConsumedData}. This can be used to
     * transform the read data into a Java primitive
     * @param length How much data to read
     * @return A {@link me.eddiep.ghost.network.packet.ConsumedData} object to allow easy transformation of the data
     * @throws IOException If there was a problem reading the data
     * @see me.eddiep.ghost.network.packet.ConsumedData
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
                pos += r;
                i += r;
            }

            return new ConsumedData(data);
        } else {
            byte[] data = new byte[length];
            System.arraycopy(this.udpData, pos, data, 0, length);
            pos += length;

            return new ConsumedData(data);
        }
    }

    /**
     * Read a single byte or the entire packet as a {@link me.eddiep.ghost.network.packet.ConsumedData}. This can be used to
     * transform the read data into a Java primitive. Whether this method reads a single byte or the entire packet depends on
     * whether this packet is reading from a {@link java.io.InputStream} or a byte array. If from a {@link java.io.InputStream}, then
     * it will return a single byte, otherwise the entire packet
     * @return A {@link me.eddiep.ghost.network.packet.ConsumedData} object to allow easy transformation of the data
     * @throws IOException If there was a problem reading the data
     * @see me.eddiep.ghost.network.packet.ConsumedData
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
        String json = GSON.toJson(obj);
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
        validateTempStream();
        tempWriter.write(val);
        return this;
    }

    /**
     * Write a byte into this packet
     * @param val The byte value
     * @return This packet
     */
    public Packet write(byte val) {
        validateTempStream();
        tempWriter.write(val);
        return this;
    }

    /**
     * Write an int into this packet
     * @param val The int value
     * @throws java.io.IOException if there was a problem writing the value
     * @return This packet
     */
    public Packet write(int val) throws IOException {
        validateTempStream();
        tempWriter.write(ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(val).array());
        return this;
    }

    /**
     * Write a float into this packet
     * @param val The float value
     * @throws java.io.IOException if there was a problem writing the value
     * @return This packet
     */
    public Packet write(float val) throws IOException {
        validateTempStream();
        tempWriter.write(ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putFloat(val).array());
        return this;
    }

    /**
     * Write a double into this packet
     * @param val The double value
     * @throws java.io.IOException if there was a problem writing the value
     * @return This packet
     */
    public Packet write(double val) throws IOException {
        validateTempStream();
        tempWriter.write(ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN).putDouble(val).array());
        return this;
    }

    /**
     * Write a long into this packet
     * @param val The long value
     * @throws java.io.IOException if there was a problem writing the value
     * @return This packet
     */
    public Packet write(long val) throws IOException {
        validateTempStream();
        tempWriter.write(ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN).putLong(val).array());
        return this;
    }

    /**
     * Write a short into this packet
     * @param val The short value
     * @throws java.io.IOException if there was a problem writing the value
     * @return This packet
     */
    public Packet write(short val) throws IOException {
        validateTempStream();
        tempWriter.write(ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN).putShort(val).array());
        return this;
    }

    /**
     * Write a String into this packet, encoded as ASCII
     * @param string The String value
     * @throws java.io.IOException if there was a problem writing the value
     * @return This packet
     */
    public Packet write(String string) throws IOException {
        validateTempStream();
        tempWriter.write(string.getBytes(Charset.forName("ASCII")));
        return this;
    }

    /**
     * Write a String into this packet encoded as a given {@link java.nio.charset.Charset}
     * @param string The String value
     * @param charset The charset to encode the String as
     * @throws java.io.IOException if there was a problem writing the value
     * @return This packet
     */
    public Packet write(String string, Charset charset) throws IOException {
        validateTempStream();
        tempWriter.write(string.getBytes(charset));
        return this;
    }

    /**
     * Write a boolean into this packet. This will write 1 byte, 1 being true and 0 being false
     * @param value The boolean value
     * @throws java.io.IOException if there was a problem writing the value
     * @return This packet
     */
    public Packet write(boolean value) throws IOException {
        validateTempStream();
        tempWriter.write(value ? (byte)1 : (byte)0);
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
    public final Packet handlePacket() throws IOException {
        onHandlePacket(client);
        return this;
    }

    /**
     * Start writing this packet with the given data
     * @param args The data for this packet
     * @return This packet
     * @throws IOException If there was a problem reading the packet
     */
    public final Packet writePacket(Object... args) throws IOException {
        onWritePacket(client, args);
        return this;
    }

    protected void onHandlePacket(C client) throws IOException {
        throw new IllegalAccessError("This packet does not handle data!");
    }

    protected void onWritePacket(C client, Object... args) throws IOException {
        throw new IllegalAccessError("This packet does not write data!");
    }
}
