package com.boxtrotstudio.ghost.matchmaking.network;

import com.boxtrotstudio.ghost.matchmaking.network.packets.OkPacket;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import com.boxtrotstudio.ghost.network.Client;

import java.io.IOException;

public abstract class TcpClient extends Client<TcpServer> {
    private ChannelHandlerContext channel;

    public TcpClient(TcpServer server) throws IOException {
        super(server);
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
    }

    @Override
    protected void onDisconnect() throws IOException {
    }

    @Override
    public void write(byte[] data) throws IOException {
        this.channel.write(Unpooled.copiedBuffer(data));
        this.channel.flush();
    }

    @Override
    public int read(byte[] into, int offset, int length) throws IOException {
        return 0;
    }

    @Override
    public void flush() throws IOException {
        this.channel.flush();
    }

    public abstract void handlePacket(byte[] rawData) throws IOException;

    public void attachChannel(ChannelHandlerContext channelHandlerContext) {
        this.channel = channelHandlerContext;
        this.channel.channel().closeFuture().addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture channelFuture) throws Exception {
                TcpClient.super.socketServer.disconnect(TcpClient.this);
            }
        });
    }

    public ChannelHandlerContext getChannel() {
        return channel;
    }
}
