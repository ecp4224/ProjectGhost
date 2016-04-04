package com.boxtrotstudio.ghost.matchmaking.network;

import com.boxtrotstudio.ghost.matchmaking.ServerConfig;
import com.boxtrotstudio.ghost.matchmaking.network.netty.TcpServerInitializer;
import com.boxtrotstudio.ghost.network.Client;
import com.boxtrotstudio.ghost.network.Server;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.concurrent.GenericProgressiveFutureListener;
import io.netty.util.concurrent.ProgressiveFuture;
import me.eddiep.jconfig.JConfig;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TcpServer extends Server {
    List<Client> connectedClients = new ArrayList<>();
    private ServerConfig config;
    private TcpServerHandler handler;

    @Override
    protected void onStart() {
        super.onStart();

        config = JConfig.newConfigObject(ServerConfig.class);
        File file = new File("server.json");
        if (!file.exists())
            config.save(file);
        else
            config.load(file);

        if (config.getServerSecret().length() != 32) {
            System.err.println("The server secret is not 32 characters!");
            System.err.println("Aborting..");
            System.exit(1);
            return;
        }

        if (config.getAdminSecret().length() != 32) {
            System.err.println("The admin secret is not 32 characters!");
            System.err.println("Aborting..");
            System.exit(2);
            return;
        }

        handler = new TcpServerHandler(this);
        final EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        final EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.DEBUG))
                    .childHandler(new TcpServerInitializer(this, handler));

            //TODO Handle future when channel is closed
            b.bind(config.getServerPort()).sync().channel().closeFuture().addListener(new GenericProgressiveFutureListener<ProgressiveFuture<Void>>() {
                @Override
                public void operationProgressed(ProgressiveFuture progressiveFuture, long l, long l1) throws Exception {
                }

                @Override
                public void operationComplete(ProgressiveFuture progressiveFuture) throws Exception {
                    bossGroup.shutdownGracefully();
                    workerGroup.shutdownGracefully();
                }
            });
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public ServerConfig getConfig() {
        return config;
    }

    public void onDisconnect(TcpClient client) throws IOException {
        System.out.println("[SERVER] " + client.getIpAddress() + " disconnected..");
        connectedClients.remove(client);

        handler._onDisconnect(client);
    }

    public void disconnect(TcpClient client) throws IOException {
        handler._disconnect(client);
    }

    public List<Client> getConnectedClients() {
        return Collections.unmodifiableList(connectedClients);
    }
}
