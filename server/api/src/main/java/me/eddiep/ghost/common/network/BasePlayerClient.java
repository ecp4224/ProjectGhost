package me.eddiep.ghost.common.network;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import me.eddiep.ghost.common.game.NetworkMatch;
import me.eddiep.ghost.common.game.Player;
import me.eddiep.ghost.common.network.packet.OkPacket;
import me.eddiep.ghost.common.network.packet.PlayerPacketFactory;
import me.eddiep.ghost.network.Client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.Socket;

public class BasePlayerClient extends Client<BaseServer> {

    private boolean isAuth;
    private Thread readerThread;
    private Socket socket;
    private int lastReadPacket = 0;

    private int lastWritePacket;
    protected Player player;
    private ChannelHandlerContext channel;

    public BasePlayerClient(BaseServer server) throws IOException {
        super(server);
    }

    /*public BasePlayerClient(Player player, Socket socket, BaseServer server) throws IOException {
        super(server);

        this.player = player;
        this.socket = socket;
        this.IpAddress = socket.getInetAddress();
        this.socketServer = server;

        this.writer = socket.getOutputStream();
        this.reader = socket.getInputStream();

        this.player.setClient(this);

        this.socket.setSoTimeout(0);
    }*/

    public Player getPlayer() {
        return player;
    }

    @Override
    public void listen() { }

    @Override
    protected void onDisconnect() throws IOException {
        if (readerThread != null) {
            readerThread.interrupt();
        }

        readerThread = null;

        if (player != null) {
            if (player.isInMatch() && !player.isSpectating()) {
                ((NetworkMatch)player.getMatch()).playerDisconnected(player);
            } else if (player.isInMatch() && player.hasStartedSpectating()) {
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
        channel.write(Unpooled.copiedBuffer(data));
        channel.flush();
    }

    @Override
    public int read(byte[] into, int offset, int length) throws IOException {
        return 0;
    }

    @Override
    public void flush() throws IOException {
        channel.flush();
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

    public void handlePacket(byte[] rawData) throws IOException {
        byte opCode = rawData[0];
        byte[] data = new byte[rawData.length - 1];

        System.arraycopy(rawData, 1, data, 0, data.length);

        PlayerPacketFactory.get(opCode, this)
                .attachPacket(data)
                .handlePacket()
                .endTCP();

    }

    public void attachChannel(ChannelHandlerContext channelHandlerContext) {
        this.channel = channelHandlerContext;
        this.channel.channel().closeFuture().addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture channelFuture) throws Exception {
                BasePlayerClient.super.socketServer.disconnect(BasePlayerClient.this);
            }
        });
    }

    public ChannelHandlerContext getChannel() {
        return channel;
    }
}
