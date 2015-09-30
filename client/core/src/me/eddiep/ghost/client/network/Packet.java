package me.eddiep.ghost.client.network;

import me.eddiep.ghost.client.utils.Global;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.util.zip.GZIPOutputStream;

public class Packet<C extends Client> {

    private byte[] udpData;
    private ByteArrayOutputStream tempWriter;
    private C client;
    private boolean ended;
    private int pos = 0;

    public Packet() {

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

    public Packet attachPacket(byte[] data) {
        this.udpData = data;
        return this;
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

    public void endTCP() {
        if (client == null)
            return;

        if (tempWriter != null) {
            try {
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
        validateTempStream();
        tempWriter.write(val);
        return this;
    }

    public Packet write(byte[] val, int offset, int length) throws IOException {
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
        if (tempWriter == null)
            throw new IllegalStateException("No data written!");

        byte[] currentData = tempWriter.toByteArray();
        tempWriter.close();
        tempWriter = null; //Reset writer

        write(currentData[0]); //Write opCode first
        write(currentData.length); //Then append size of packet to front of packet
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
