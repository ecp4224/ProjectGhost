package me.eddiep.ghost.client.network;

import me.eddiep.ghost.client.Ghost;
import me.eddiep.ghost.client.handlers.GameHandler;
import me.eddiep.ghost.client.network.packets.PacketFactory;
import me.eddiep.ghost.client.network.packets.ReadyPacket;
import me.eddiep.ghost.client.network.packets.UdpSessionPacket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.*;

public class PlayerClient implements Client {
    private Socket socket;
    private InputStream inputStream;
    private OutputStream outputStream;

    private DatagramSocket udpSocket;
    private InetAddress address;
    private short port;

    private Thread tcpThread, udpThread;

    private GameHandler game;
    public int lastRead;

    public static PlayerClient connect(String ip, GameHandler game) throws IOException {
        short port = 2546;
        String[] temp = ip.split(":");
        if (temp.length > 1) {
            port = Short.parseShort(temp[1]);
            ip = temp[0];
        }

        PlayerClient client = new PlayerClient();
        client.address = InetAddress.getByName(ip);
        client.port = port;
        client.game = game;
        client.socket = new Socket(ip, port);
        client.setup();

        return client;
    }

    public static PlayerClient connect(String ip) throws IOException {
        short port = 2546;
        String[] temp = ip.split(":");
        if (temp.length > 1) {
            port = Short.parseShort(temp[1]);
            ip = temp[0];
        }

        PlayerClient client = new PlayerClient();
        client.address = InetAddress.getByName(ip);
        client.port = port;
        client.socket = new Socket(ip, port);
        client.setup();

        return client;
    }

    private void setup() throws IOException {
        inputStream = socket.getInputStream();
        outputStream = socket.getOutputStream();

        tcpThread = new Thread(TCP_READ_THREAD);
        udpThread = new Thread(UDP_READ_THREAD);

        tcpThread.start();
        udpThread.start();
    }

    public void connectUDP() throws IOException {
        udpSocket = new DatagramSocket();

        UdpSessionPacket packet = new UdpSessionPacket();
        packet.writePacket(this);
    }

    private PlayerClient() { }

    public GameHandler getGame() {
        return game;
    }

    @Override
    public void writeUDP(byte[] data) throws IOException {
        DatagramPacket packet = new DatagramPacket(data, 0, data.length, address, port);
        udpSocket.send(packet);
    }


    @Override
    public void write(byte[] data) throws IOException {
        outputStream.write(data, 0, data.length);
    }

    @Override
    public void flush() throws IOException {
        outputStream.flush();
    }

    @Override
    public int read(byte[] data, int index, int length) throws IOException {
        return inputStream.read(data, index, length);
    }

    @Override
    public void disconnect() throws IOException {
        socket.close();
        udpSocket.close();

        tcpThread.interrupt();
        udpThread.interrupt();

        inputStream = null;
        outputStream = null;
        socket = null;
        udpSocket = null;
    }

    private final Runnable TCP_READ_THREAD = new Runnable() {
        @Override
        public void run() {
           Thread.currentThread().setName("TCP-Read-Thread");
            while (socket.isConnected()) {
                try {
                    int opCode = inputStream.read();
                    if (opCode == -1)
                        break;

                    Packet<PlayerClient> p = PacketFactory.getPacket(opCode);
                    if (p != null)
                        p.handlePacket(PlayerClient.this);
                    else
                        System.err.println("UNKNOWN OPCODE " + opCode);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    private final Runnable UDP_READ_THREAD = new Runnable() {
        @Override
        public void run() {
            Thread.currentThread().setName("UDP-Read-Thread");
            while (udpSocket.isConnected()) {
                try {
                    byte[] buffer = new byte[1024];

                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    udpSocket.receive(packet);

                    byte[] data = packet.getData();

                    byte[] trueData = new byte[data.length - 1];
                    System.arraycopy(data, 0, trueData, 0, trueData.length);

                    Packet<PlayerClient> p = PacketFactory.getPacket(data[0], trueData);
                    if (p != null)
                        p.handlePacket(PlayerClient.this);
                    else
                        System.err.println("UNKNOWN OPCODE " + data[0]);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    public synchronized void setOk(boolean val) {
        this.isOk = val;
        this.gotOk = true;
        super.notifyAll();
    }

    private boolean gotOk = false;
    private boolean isOk = false;
    public synchronized boolean ok() throws InterruptedException {
        while (true) {
            if (gotOk)
                break;

            super.wait(0L);
        }

        gotOk = false;
        return isOk;
    }

    public void setReady(boolean b) {
        Ghost.isReady = b;

        try {
            ReadyPacket packet = new ReadyPacket();
            packet.writePacket(this, Ghost.isReady);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
