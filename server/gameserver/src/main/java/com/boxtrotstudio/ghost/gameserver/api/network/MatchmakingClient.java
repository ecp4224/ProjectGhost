package com.boxtrotstudio.ghost.gameserver.api.network;

import com.boxtrotstudio.ghost.common.network.BaseServer;
import com.boxtrotstudio.ghost.gameserver.api.network.packets.GameServerAuthPacket;
import com.boxtrotstudio.ghost.gameserver.api.network.packets.MatchmakingPacketFactory;
import com.boxtrotstudio.ghost.network.Client;
import com.boxtrotstudio.ghost.network.packet.Packet;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;

public class MatchmakingClient extends Client<BaseServer> {

    private Thread readerThread;
    private Socket socket;

    private OutputStream writer;
    private InputStream reader;

    public MatchmakingClient(Socket socket, BaseServer server) throws IOException {
        super(server);

        this.socket = socket;

        this.writer = socket.getOutputStream();
        this.reader = socket.getInputStream();

        this.IpAddress = socket.getInetAddress();
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
    public boolean isConnected() {
        return socket != null && !socket.isClosed() && socket.isConnected();
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

    public void auth(String secret, long ID) throws IOException, InterruptedException {
        GameServerAuthPacket packet = new GameServerAuthPacket(this);
        packet.writePacket(secret, ID);

        boolean isOk = waitForOk();
        if (!isOk)
            throw new IOException("Matchmaking server rejected auth! See Matchmaking log for more info!");
    }

    private boolean okVal;
    private boolean receive = false;
    public synchronized void receiveOk(boolean ok) {
        okVal = ok;
        receive = true;
        super.notifyAll();
    }

    public synchronized boolean waitForOk() throws InterruptedException {
        receive = false;
        while (true) {
            if (receive)
                break;

            super.wait(0L);
        }

        return okVal;
    }

    public void dispose() {
        writer = null;
        reader = null;
        socket = null;
        connected = false;
        if (readerThread != null) {
            readerThread.interrupt();
        }
        readerThread = null;
    }

    private class Reader extends Thread {

        @Override
        public void run() {
            Thread.currentThread().setName("MatchMaking-" + getIpAddress() + "-Reader");
            try {
                while (connected) {
                    int readValue = reader.read();

                    if (readValue == -1) {
                        disconnect();
                        return;
                    }
                    byte opCode = (byte) readValue;
                    Packet<BaseServer, MatchmakingClient> packet = MatchmakingPacketFactory.get(opCode);
                    if (packet != null)
                        packet.handlePacket(MatchmakingClient.this).endTCP();
                }
            } catch (SocketException e) {
                if (!e.getMessage().contains("Connection reset")) {
                    e.printStackTrace();
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    socketServer.disconnect(MatchmakingClient.this);
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
    }
}
