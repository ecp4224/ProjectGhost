package com.boxtrotstudio.ghost.common.network.netty;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.nio.NioSocketChannel;
import com.boxtrotstudio.ghost.common.network.BaseServer;
import com.boxtrotstudio.ghost.common.network.TcpServerHandler;

public class TcpServerInitializer extends ChannelInitializer<NioSocketChannel> {

    private final BaseServer server;
    private final TcpServerHandler handler;

    public TcpServerInitializer(BaseServer server, TcpServerHandler handler) {
        this.server = server;
        this.handler = handler;
    }

    @Override
    protected void initChannel(NioSocketChannel nioSocketChannel) throws Exception {
        ChannelPipeline pipeline = nioSocketChannel.pipeline();

        pipeline.addLast(new PacketDecoder());
        pipeline.addLast(handler);
    }
}
