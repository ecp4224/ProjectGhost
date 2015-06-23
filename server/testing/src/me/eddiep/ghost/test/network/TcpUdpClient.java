package me.eddiep.ghost.test.network;

import me.eddiep.ghost.network.Client;
import me.eddiep.ghost.test.game.ActiveMatch;
import me.eddiep.ghost.test.game.Player;
import me.eddiep.ghost.test.network.packet.PacketFactory;
import me.eddiep.ghost.test.network.packet.OkPacket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class TcpUdpClient extends Client<TcpUdpServer> {

    private Thread writerThread;
    private Thread readerThread;
    private Socket socket;
    private int lastReadPacket = 0;

    private OutputStream writer;
    private InputStream reader;

    protected List<byte[]> tcp_packet_queue = Collections.synchronizedList(new LinkedList<byte[]>());
    private int lastWritePacket;
    private Player player;
    public TcpUdpClient(Player player, Socket socket, TcpUdpServer server) throws IOException {
        super(player, server);

        this.player = player;
        this.socket = socket;
        this.IpAddress = socket.getInetAddress();
        this.socketServer = server;

        this.writer = socket.getOutputStream();
        this.reader = socket.getInputStream();
    }

    public Player getPlayer() {
        return player;
    }

    @Override
    public void listen() {
        if (reader == null)
            return;

        writerThread = new Writer();
        writerThread.start();
        readerThread = new Reader();
        readerThread.start();
    }

    @Override
    protected void onDisconnect() throws IOException {
        if (writerThread != null) {
            writerThread.interrupt();
            try {
                writerThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        if (readerThread != null) {
            readerThread.interrupt();
        }

        readerThread = null;
        writerThread = null;

        if (player != null) {
            if (player.isInQueue()) {
                player.getQueue().removeUserFromQueue(player);
            } else if (player.isInMatch()) {
                ((ActiveMatch)player.getMatch()).playerDisconnected(player);
            }

        }
        player = null;
        if (socket != null && !socket.isClosed())
            socket.close();
        socket = null;
    }

    @Override
    public void write(byte[] data) throws IOException {
        writer.write(data);
    }

    @Override
    public int read(byte[] into, int offset, int length) throws IOException {
        return reader.read(into, offset, length);
    }

    public Socket getSocket() {
        return socket;
    }

    public Client sendOk() throws IOException {
        return sendOk(true);
    }

    public Client sendOk(boolean value) throws IOException {
        OkPacket packet = new OkPacket(this);
        packet.writePacket(value);
        return this;
    }

    public void sendTCPPacket(byte[] data) {
        tcp_packet_queue.add(data);
    }

    protected boolean sendTCPNextPacket() throws IOException {
        if (tcp_packet_queue.isEmpty())
            return false;
        byte[] packet = tcp_packet_queue.remove(0);
        if (packet == null)
            return false;
        writer.write(packet);
        return true;
    }

    public void processUdpPacket(DatagramPacket recievePacket) throws IOException {
        byte[] rawData = recievePacket.getData();
        byte opCode = rawData[0];
        byte[] data = new byte[recievePacket.getLength() - 1];

        System.arraycopy(rawData, 1, data, 0, data.length);

        PacketFactory.get(opCode, this, data).handlePacket().endUDP();
    }

    public int getLastReadPacket() {
        return lastReadPacket;
    }

    public void setLastReadPacket(int number) {
        this.lastReadPacket = number;
    }

    public InputStream getInputStream() {
        return reader;
    }

    public OutputStream getOutputStream() {
        return writer;
    }

    public int getLastWritePacket() {
        return lastWritePacket;
    }

    public void setLastWritePacket(int lastWritePacket) {
        this.lastWritePacket = lastWritePacket;
    }

    private long latency;
    private long pingStart;
    private int pingNumber;
    public void startPingTimer(int pingCount) {
        pingStart = System.currentTimeMillis();
    }

    public void endPingTimer(int pingRecieved) {
        if (pingRecieved > pingNumber) {
            latency = (System.currentTimeMillis() - pingStart) / 2;
        }
    }

    public long getLatency() {
        return latency;
    }

    private class Writer extends Thread {

        @Override
        public void run() {
            Thread.currentThread().setName("Client-" + getPlayer().getSession() + "-Writer");
            while (socketServer.isRunning() && connected) {
                try {
                    while (sendTCPNextPacket());

                    Thread.sleep(2);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException ignored) {
                }
            }

            try {
                while (sendTCPNextPacket()); //Be sure all packets get sent
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private class Reader extends Thread {

        @Override
        public void run() {
            Thread.currentThread().setName("Client-" + getPlayer().getSession() + "-Reader");
            try {
                while (socketServer.isRunning() && connected) {
                    int readValue = reader.read();

                    if (readValue == -1) {
                        disconnect();
                        return;
                    }

                    byte opCode = (byte) readValue;
                    PacketFactory.get(opCode, TcpUdpClient.this).handlePacket().endTCP();

                }
            } catch (SocketException e) {
                if (!e.getMessage().contains("Connection reset")) {
                    e.printStackTrace();
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    socketServer.disconnect(TcpUdpClient.this);
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
    }
}
