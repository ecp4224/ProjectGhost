package me.eddiep.ghost.common.network;

import me.eddiep.ghost.common.game.NetworkMatch;
import me.eddiep.ghost.common.game.Player;
import me.eddiep.ghost.common.network.packet.OkPacket;
import me.eddiep.ghost.common.network.packet.PlayerPacketFactory;
import me.eddiep.ghost.network.Client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;

public class BasePlayerClient extends Client<BaseServer> {

    private boolean isAuth;
    private Thread readerThread;
    private Socket socket;
    private int lastReadPacket = 0;

    private OutputStream writer;
    private InputStream reader;

    private int lastWritePacket;
    protected Player player;

    public BasePlayerClient(BaseServer server) throws IOException {
        super(server);
    }

    public BasePlayerClient(Player player, Socket socket, BaseServer server) throws IOException {
        super(server);

        this.player = player;
        this.socket = socket;
        this.IpAddress = socket.getInetAddress();
        this.socketServer = server;

        this.writer = socket.getOutputStream();
        this.reader = socket.getInputStream();

        this.player.setClient(this);

        this.socket.setSoTimeout(0);
    }

    public Player getPlayer() {
        return player;
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

        if (player != null) {
            if (player.isInMatch() && !player.isSpectating()) {
                ((NetworkMatch)player.getMatch()).playerDisconnected(player);
            } else if (player.isInMatch()) {
                player.stopSpectating();
            }

            player.disconnected();
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

    @Override
    public void flush() throws IOException {
        writer.flush();
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

    public void processUdpPacket(DatagramPacket recievePacket) throws IOException {
        byte[] rawData = recievePacket.getData();
        byte opCode = rawData[0];
        byte[] data = new byte[recievePacket.getLength() - 1];

        System.arraycopy(rawData, 1, data, 0, data.length);

        PlayerPacketFactory.get(opCode, this, data).handlePacket().endUDP();
    }

    public void processTcpPacket(byte opCode) throws IOException {
        PlayerPacketFactory.get(opCode, this).handlePacket().endTCP();
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

    public void attachPlayer(Player player, InetAddress address) {
        this.player = player;
        this.player.setClient(this);

        this.IpAddress = address;
    }

    public void handlePacket(byte[] data) {

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
                    processTcpPacket(opCode);
                }
            } catch (SocketException e) {
                if (!e.getMessage().contains("Connection reset")) {
                    e.printStackTrace();
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    socketServer.disconnect(BasePlayerClient.this);
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
    }
}
