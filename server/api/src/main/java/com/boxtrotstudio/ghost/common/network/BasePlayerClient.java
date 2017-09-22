package com.boxtrotstudio.ghost.common.network;

import com.boxtrotstudio.ghost.network.packet.Packet;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import com.boxtrotstudio.ghost.common.game.NetworkMatch;
import com.boxtrotstudio.ghost.common.game.Player;
import com.boxtrotstudio.ghost.common.network.packet.DisconnectReasonPacket;
import com.boxtrotstudio.ghost.common.network.packet.OkPacket;
import com.boxtrotstudio.ghost.common.network.packet.PlayerPacketFactory;
import com.boxtrotstudio.ghost.network.Client;

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
    private long lastPing;

    public BasePlayerClient(BaseServer server) throws IOException {
        super(server);
    }

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

        Packet packet = PlayerPacketFactory.get(opCode);
        if (packet == null) {
            getServer().getLogger().error("Invalid opcode sent via UDP: " + opCode + ", data length=" + data.length);
            return;
        }
        packet.handlePacket(this, data);
        packet.endUDP();
    }

    public int getLastReadPacket() {
        return lastReadPacket;
    }

    public void setLastReadPacket(int number) {
        this.lastReadPacket = number;
    }

    public int getWriteNumber() {
        lastWritePacket++;
        return lastWritePacket;
    }

    private long latency;
    private long pingStart;
    private int pingNumber;
    public void startPingTimer(int pingCount) {
        pingStart = System.currentTimeMillis();
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

        Packet packet = PlayerPacketFactory.get(opCode);
        if (packet == null)
            throw new IllegalAccessError("Invalid packet!");

        //System.err.println("HANDLE " + packet.getClass().getSimpleName() + " PACKET FROM " + getIpAddress() + ":" + getPort());

        packet.handlePacket(this, data);
        packet.endTCP();

    }

    public void attachChannel(ChannelHandlerContext channelHandlerContext) {
        this.channel = channelHandlerContext;
        this.channel.channel().closeFuture().addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture channelFuture) throws Exception {
                BasePlayerClient.super.socketServer.onDisconnect(BasePlayerClient.this);
            }
        });
    }

    public ChannelHandlerContext getChannel() {
        return channel;
    }

    public void kick() throws IOException {
        kick("No reason specified");
    }

    public void kick(String reason) throws IOException {
        DisconnectReasonPacket packet = new DisconnectReasonPacket(this);
        packet.writePacket(reason);

        this.getServer().disconnect(this);
    }

    public void onPing() {
        if (lastPing != 0L) {
            long time = System.currentTimeMillis() - lastPing;
            latency = time - 5000; //ping should be every 5 seconds, anything more than that is latency
        }

        lastPing = System.currentTimeMillis();
    }
}
