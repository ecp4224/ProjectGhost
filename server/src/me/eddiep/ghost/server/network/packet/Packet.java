package me.eddiep.ghost.server.network.packet;

import me.eddiep.ghost.server.network.Client;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;

public abstract class Packet {
    private byte[] udpData;
    private InputStream reader;
    private OutputStream writer;
    private ByteArrayOutputStream tempWriter;
    private Client client;
    private boolean ended;
    private int pos = 0;

    /**
     * Create a new packet processor that reads data dynamically. Use this constructor for TCP Packets
     * @param client The client this packet came from
     */
    public Packet(Client client) {
        this.client = client;
        this.reader = client.getInputStream();
        this.writer = client.getOutputStream();
    }

    /**
     * Create a new packet processor that reads data that is provided. Use this constructor for UDP Packets
     * @param client The client this packet came from
     * @param data The packet to process
     */
    public Packet(Client client, byte[] data) {
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

    public void endTCP() {
        if (tempWriter != null) {
            try {
                byte[] data = tempWriter.toByteArray();
                writer.write(data);
                tempWriter.close();
                writer.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        end();
    }

    public DatagramPacket endUDP() {
        DatagramPacket packet = null;
        if (tempWriter != null) {
            try {
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

    private void end() {
        reader = null;
        writer = null;
        tempWriter = null;
        client = null;
        ended = true;
    }

    protected ConsumedData consume(int length) throws IOException {
        if (ended)
            throw new IOException("This packet has already ended!");

        if (reader != null) {
            byte[] data = new byte[length];
            int r = reader.read(data, 0, length);
            pos += r;

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

        if (reader != null) {
            byte[] data = new byte[1];
            int r = reader.read(data, 0, 1);
            pos += data.length;
            return new ConsumedData(data);
        } else {
            int toRead = udpData.length - pos;
            return consume(toRead);
        }
    }

    public Packet write(byte val) {
        validateTempStream();
        tempWriter.write(val);
        return this;
    }

    public Packet write(int val) throws IOException {
        validateTempStream();
        tempWriter.write(ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(val).array());
        return this;
    }

    public Packet write(float val) throws IOException {
        validateTempStream();
        tempWriter.write(ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putFloat(val).array());
        return this;
    }

    public Packet write(double val) throws IOException {
        validateTempStream();
        tempWriter.write(ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN).putDouble(val).array());
        return this;
    }

    public Packet write(long val) throws IOException {
        validateTempStream();
        tempWriter.write(ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN).putLong(val).array());
        return this;
    }

    public Packet write(short val) throws IOException {
        validateTempStream();
        tempWriter.write(ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN).putShort(val).array());
        return this;
    }

    public Packet write(String string) throws IOException {
        validateTempStream();
        tempWriter.write(string.getBytes(Charset.forName("ASCII")));
        return this;
    }

    public Packet write(String string, Charset charset) throws IOException {
        validateTempStream();
        tempWriter.write(string.getBytes(charset));
        return this;
    }

    public Packet write(boolean value) throws IOException {
        validateTempStream();
        tempWriter.write(value ? (byte)1 : (byte)0);
        return this;
    }

    private void validateTempStream() {
        if (tempWriter == null)
            tempWriter = new ByteArrayOutputStream();
    }

    public final Packet handlePacket() throws IOException {
        onHandlePacket(client);
        return this;
    }

    public final Packet writePacket(Object... args) throws IOException {
        onWritePacket(client, args);
        return this;
    }

    protected void onHandlePacket(Client client) throws IOException {
        throw new IllegalAccessError("This packet does not handle data!");
    }

    protected void onWritePacket(Client client, Object... args) throws IOException {
        throw new IllegalAccessError("This packet does not write data!");
    }
}
