package me.eddiep.ghost.matchmaking.network.netty;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.nio.NioSocketChannel;
import me.eddiep.ghost.matchmaking.network.TcpServer;
import me.eddiep.ghost.matchmaking.network.TcpServerHandler;

public class TcpServerInitializer extends ChannelInitializer<NioSocketChannel> {

    private final TcpServer server;
    private final TcpServerHandler handler;

    public TcpServerInitializer(TcpServer server, TcpServerHandler handler) {
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
