package me.eddiep.ghost.server.packet;

import com.sun.xml.internal.bind.v2.runtime.reflect.Lister;
import me.eddiep.ghost.server.network.Client;
import me.eddiep.ghost.server.packet.impl.ReadyPacket;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.HashMap;

public abstract class Packet {
    private static HashMap<Byte, Class<? extends Packet>> packets = new HashMap<>();

    static {
        packets.put((byte) 0x03, ReadyPacket.class);

    }

    private InputStream reader;
    private OutputStream writer;
    private ByteArrayOutputStream tempWriter;
    private Client client;
    private boolean ended;
    private int pos = 0;
    public Packet(Client client) {
        this.client = client;
        this.reader = client.getInputStream();
        this.writer = client.getOutputStream();
    }

    public int getPosition() {
        return pos;
    }

    public void setPosition(int pos) {
        this.pos = pos;
    }

    public boolean isEnded() {
        return ended;
    }

    public void end() {
        reader = null;
        ended = true;
        if (tempWriter != null) {
            try {
                tempWriter.writeTo(writer);
                tempWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        writer = null;
    }

    public ConsumedData consume(int length) throws IOException {
        if (ended)
            throw new IOException("This packet has already ended!");

        byte[] data = new byte[length];
        int r = reader.read(data, 0, length);
        pos += r;

        return new ConsumedData(data);
    }

    public ConsumedData consume() throws IOException {
        if (ended)
            throw new IOException("This packet has already ended!");

        byte[] data = new byte[1];
        int r = reader.read(data, 0, 1);
        pos += data.length;
        return new ConsumedData(data);
    }

    public Packet write(byte val) {
        validateTempStream();
        tempWriter.write(val);
        return this;
    }

    public Packet write(int val) throws IOException {
        validateTempStream();
        tempWriter.write(ByteBuffer.allocate(4).putInt(val).array());
        return this;
    }

    public Packet wirte(float val) throws IOException {
        validateTempStream();
        tempWriter.write(ByteBuffer.allocate(4).putFloat(val).array());
        return this;
    }

    public Packet write(double val) throws IOException {
        validateTempStream();
        tempWriter.write(ByteBuffer.allocate(8).putDouble(val).array());
        return this;
    }

    public Packet write(long val) throws IOException {
        validateTempStream();
        tempWriter.write(ByteBuffer.allocate(8).putLong(val).array());
        return this;
    }

    public Packet write(short val) throws IOException {
        validateTempStream();
        tempWriter.write(ByteBuffer.allocate(2).putInt(val).array());
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

    private void validateTempStream() {
        tempWriter = new ByteArrayOutputStream();
    }

    public final Packet handlePacket() throws IOException {
        onHandlePacket(client);
        return this;
    }

    public final Packet writePacket(Object... args) throws IOException {
        onWritePacket(client);
        return this;
    }

    protected void onHandlePacket(Client client) throws IOException { }

    protected void onWritePacket(Client client, Object... args) throws IOException { }

    public static Packet get(byte opCode, Client client) {
        try {
            return packets.get(opCode).getConstructor(Client.class).newInstance(client);
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        return null;
    }
}
