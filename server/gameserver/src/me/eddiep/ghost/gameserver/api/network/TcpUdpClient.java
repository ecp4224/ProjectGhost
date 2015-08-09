package me.eddiep.ghost.gameserver.api.network;

import me.eddiep.ghost.gameserver.api.game.player.PlayerFactory;
import me.eddiep.ghost.gameserver.api.network.packets.OkPacket;
import me.eddiep.ghost.gameserver.api.network.packets.PacketFactory;
import me.eddiep.ghost.gameserver.api.game.player.Player;
import me.eddiep.ghost.network.Client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.Socket;
import java.net.SocketException;

public class TcpUdpClient extends Client<TcpUdpServer> {
    private Thread readerThread;
    private Socket socket;

    private OutputStream writer;
    private InputStream reader;
    private int lastReadPacket;
    private int lastWritePacket;

    private Player player;

    public TcpUdpClient(Player user, Socket socket, TcpUdpServer server) throws IOException {
        super(server);

        this.player = user;

        this.socket = socket;

        this.writer = socket.getOutputStream();
        this.reader = socket.getInputStream();

        this.IpAddress = socket.getInetAddress();

        this.player.setClient(this);
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

        if (player != null) {
            if (player.isInMatch()) {
                ((NetworkMatch)player.getMatch()).playerDisconnected(player);
            } else {
                PlayerFactory.invalidateSession(player);
            }

            player.disconnected();
        }
        player = null;

        readerThread = null;

        if (socket != null && !socket.isClosed())
            socket.close();
        socket = null;
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

    public int getLastWritePacket() {
        return lastWritePacket;
    }

    public void setLastWritePacket(int lastWritePacket) {
        this.lastWritePacket = lastWritePacket;
    }

    public int getLastReadPacket() {
        return lastReadPacket;
    }

    public void setLastReadPacket(int number) {
        this.lastReadPacket = number;
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

    public Player getPlayer() {
        return player;
    }

    public void processUdpPacket(DatagramPacket recievePacket) throws IOException {
        byte[] rawData = recievePacket.getData();
        byte opCode = rawData[0];
        byte[] data = new byte[recievePacket.getLength() - 1];

        System.arraycopy(rawData, 1, data, 0, data.length);

        PacketFactory.get(opCode, this, data).handlePacket().endUDP();
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
