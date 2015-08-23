package me.eddiep.ghost.matchmaking.network;

import me.eddiep.ghost.matchmaking.network.packets.OkPacket;
import me.eddiep.ghost.network.Client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;

public abstract class TcpClient extends Client<TcpServer> {
    private Thread readerThread;
    private Socket socket;

    private OutputStream writer;
    protected InputStream reader;

    public TcpClient(Socket socket, TcpServer server) throws IOException {
        super(server);

        this.socket = socket;

        this.writer = socket.getOutputStream();
        this.reader = socket.getInputStream();

        this.IpAddress = socket.getInetAddress();

        this.socket.setSoTimeout(0);
    }

    public Client sendOk() throws IOException {
        return sendOk(true);
    }

    public Client sendOk(boolean value) throws IOException {
        OkPacket packet = new OkPacket(this);
        packet.writePacket(value);
        return this;
    }

    @Override
    public void listen() {
        if (reader == null)
            return;

        readerThread = new Reader();
        readerThread.start();
    }

    @Override
    protected void onDisconnect() throws IOException {
        if (readerThread != null) {
            readerThread.interrupt();
        }

        readerThread = null;

        if (socket != null && !socket.isClosed())
            socket.close();
        socket = null;
    }

    @Override
    public void write(byte[] data) throws IOException {
        this.writer.write(data);
    }

    @Override
    public int read(byte[] into, int offset, int length) throws IOException {
        return this.reader.read(into, offset, length);
    }

    @Override
    public void flush() throws IOException {
        this.writer.flush();
    }

    public Socket getSocket() {
        return socket;
    }

    public InputStream getInputStream() {
        return reader;
    }

    public OutputStream getOutputStream() {
        return writer;
    }

    public abstract void handlePacket(byte opCode) throws IOException;

    private class Reader extends Thread {

        @Override
        public void run() {
            Thread.currentThread().setName("Client-" + getIpAddress() + "-Reader");
            try {
                while (socketServer.isRunning() && connected) {
                    int readValue = reader.read();

                    if (readValue == -1) {
                        disconnect();
                        return;
                    }
                    byte opCode = (byte) readValue;
                    handlePacket(opCode);
                }
            } catch (SocketException e) {
                if (!e.getMessage().contains("Connection reset")) {
                    e.printStackTrace();
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    socketServer.disconnect(TcpClient.this);
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
    }
}