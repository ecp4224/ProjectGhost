package com.boxtrotstudio.ghost.client.network;

import com.boxtrotstudio.ghost.client.Ghost;
import com.boxtrotstudio.ghost.client.handlers.GameHandler;
import com.boxtrotstudio.ghost.client.handlers.scenes.BlurredScene;
import com.boxtrotstudio.ghost.client.handlers.scenes.TextOverlayScene;
import com.boxtrotstudio.ghost.client.network.packets.PacketFactory;
import com.boxtrotstudio.ghost.client.network.packets.ReadyPacket;
import com.boxtrotstudio.ghost.client.network.packets.UdpSessionPacket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.*;
import java.util.concurrent.TimeoutException;

public class PlayerClient implements Client {
    private Socket socket;
    private InputStream inputStream;
    private OutputStream outputStream;

    private DatagramSocket udpSocket;
    private InetAddress address;
    private short port;
    private String ip;

    private Thread tcpThread, udpThread;

    private GameHandler game;
    public int lastRead;
    public int sendCount;
    private boolean validated;

    public static PlayerClient connect(String ip, GameHandler game) throws UnknownHostException {
        short port = 2546;
        String[] temp = ip.split(":");
        if (temp.length > 1) {
            port = Short.parseShort(temp[1]);
            ip = temp[0];
        }

        PlayerClient client = new PlayerClient();
        client.address = InetAddress.getByName(ip);
        client.port = port;
        client.ip = ip;
        client.game = game;
        try {
            client.socket = new Socket();
            client.socket.connect(new InetSocketAddress(ip, port + 1), 5000);
            client.setup();
            game.setDisconnected(false);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return client;
    }

    public static PlayerClient connect(String ip) throws UnknownHostException {
        short port = 2546;
        String[] temp = ip.split(":");
        if (temp.length > 1) {
            port = Short.parseShort(temp[1]);
            ip = temp[0];
        }

        PlayerClient client = new PlayerClient();
        client.address = InetAddress.getByName(ip);
        client.port = port;
        try {
            client.socket = new Socket();
            client.socket.connect(new InetSocketAddress(ip, port + 1), 5000);
            client.setup();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return client;
    }

    public boolean isConnected() {
        return socket != null && socket.isConnected();
    }

    private void setup() throws IOException {
        inputStream = socket.getInputStream();
        outputStream = socket.getOutputStream();

        tcpThread = new Thread(TCP_READ_THREAD);
        tcpThread.start();
    }

    public String getIP() {
        return ip;
    }

    public short getPort() {
        return port;
    }

    public void connectUDP(String session) throws IOException {
        udpSocket = new DatagramSocket();
        udpSocket.connect(address, port);

        UdpSessionPacket packet = new UdpSessionPacket();
        packet.writePacket(this, session);
    }

    public void acceptUDPPackets() {
        udpThread = new Thread(UDP_READ_THREAD);
        udpThread.start();
    }

    private PlayerClient() { }

    public GameHandler getGame() {
        return game;
    }

    public void setGame(GameHandler game) {
        this.game = game;
    }

    public Socket getSocket() {
        return socket;
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
        if (!isConnected())
            return;

        socket.close();

        if (udpSocket != null)
            udpSocket.close();

        tcpThread.interrupt();

        if (udpThread != null)
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
                    if (e.getMessage().equals("Connection reset")) {
                        BlurredScene scene = new BlurredScene(game.world, 17f);
                        scene.requestOrder(-1);
                        TextOverlayScene scene2 = new TextOverlayScene("DISCONNECTED", "Attempting to reconnect", true);
                        game.world.replaceWith(scene);
                        Ghost.getInstance().addScene(scene2);
                        game.setDisconnected(true);
                        game.setDissconnectScene(scene);
                        game.setDissconnectScene2(scene2);
                        break;
                    }
                    e.printStackTrace();
                }
            }
        }
    };

    private final Runnable UDP_READ_THREAD = new Runnable() {
        @Override
        public void run() {
            Thread.currentThread().setName("UDP-Read-Thread");
            while (socket.isConnected()) {
                try {
                    byte[] buffer = new byte[1024];

                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    udpSocket.receive(packet);

                    byte opCode = packet.getData()[0];

                    byte[] data = new byte[packet.getLength() - 1];
                    System.arraycopy(packet.getData(), packet.getOffset() + 1, data, 0, data.length);

                    Packet<PlayerClient> p = PacketFactory.getPacket(opCode, data);
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

    public synchronized boolean ok(long timeout) throws InterruptedException, TimeoutException {
        if (gotOk)
            return isOk;

        super.wait(timeout);

        if (!gotOk)
            throw new TimeoutException();

        gotOk = false;
        return isOk;
    }

    private boolean gotOk = false;
    private boolean isOk = false;
    public boolean ok() throws InterruptedException {
        try {
            return ok(0L);
        } catch (TimeoutException e) {
            e.printStackTrace(); //can never happen
        }
        return false;
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

    public boolean getIsValidated() {
        return validated;
    }

    public void setIsValidated(boolean val) {
        this.validated = val;
    }
}
