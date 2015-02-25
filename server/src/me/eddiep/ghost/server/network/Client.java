package me.eddiep.ghost.server.network;

import me.eddiep.ghost.server.Server;
import me.eddiep.ghost.server.TcpUdpServer;
import me.eddiep.ghost.server.packet.Packet;
import me.eddiep.ghost.server.packet.impl.OkPacket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class Client {
    private Player player;
    private InetAddress IpAddress;
    private int port;
    private TcpUdpServer socketServer;
    private Server httpServer;
    private boolean connected = true;
    private Thread writerThread;
    private Thread readerThread;
    private Socket socket;
    private int lastPacketNumber = 0;

    private OutputStream writer;
    private InputStream reader;

    protected List<byte[]> tcp_packet_queue = Collections.synchronizedList(new LinkedList<byte[]>());

    public Client(Player player, Socket socket, TcpUdpServer server) throws IOException {
        this.player = player;
        this.socket = socket;
        this.socketServer = server;

        this.writer = socket.getOutputStream();
        this.reader = socket.getInputStream();

        this.player.setClient(this);
    }

    public void listen() {
        if (reader == null)
            return;

        writerThread = new Writer();
        writerThread.start();
        readerThread = new Reader();
        readerThread.start();
    }

    public InetAddress getIpAddress() {
        return IpAddress;
    }

    public int getPort() {
        return port;
    }

    public Player getPlayer() {
        return player;
    }

    public void disconnect() {
        connected = false;
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
    }

    public Client sendOk() throws IOException {
        return sendOk(true);
    }

    public Client sendOk(boolean value) throws IOException {
        OkPacket packet = new OkPacket(this);
        packet.writePacket(value);
        return this;
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

    public void processUdpPacket(DatagramPacket recievePacket) {
        
    }

    public int getLastPacketNumber() {
        return lastPacketNumber;
    }

    public void setLastPacketNumber(int number) {
        this.lastPacketNumber = number;
    }

    public InputStream getInputStream() {
        return reader;
    }

    public OutputStream getOutputStream() {
        return writer;
    }

    public TcpUdpServer getServer() {
        return socketServer;
    }

    private class Writer extends Thread {

        @Override
        public void run() {
            Thread.currentThread().setName("Client-" + IpAddress.getCanonicalHostName() + "-Writer");
            while (socketServer.isRunning() && connected) {
                try {
                    while (sendTCPNextPacket());

                    Thread.sleep(2);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
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
            Thread.currentThread().setName("Client-" + IpAddress.getCanonicalHostName() + "-Reader");
            while (socketServer.isRunning() && connected) {
                try {
                    int readValue = reader.read();

                    if (readValue == -1) {
                        disconnect();
                        return;
                    }

                    byte opCode = (byte)readValue;
                    Packet.get(opCode, Client.this).handlePacket().endTCP();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
