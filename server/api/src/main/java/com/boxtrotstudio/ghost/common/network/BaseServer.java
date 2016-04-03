package com.boxtrotstudio.ghost.common.network;

import com.boxtrotstudio.ghost.common.BaseServerConfig;
import com.boxtrotstudio.ghost.common.game.NetworkMatch;
import com.boxtrotstudio.ghost.common.game.PlayerFactory;
import com.boxtrotstudio.ghost.common.network.netty.TcpServerInitializer;
import com.boxtrotstudio.ghost.network.Server;
import com.boxtrotstudio.ghost.utils.tick.TickerPool;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.concurrent.GenericProgressiveFutureListener;
import io.netty.util.concurrent.ProgressiveFuture;
import com.boxtrotstudio.ghost.common.game.Player;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.Charset;
import java.util.HashMap;

public class BaseServer extends Server {
    private static final int PORT = 2546;

    protected DatagramSocket udpServerSocket;
    protected Thread udpThread;

    private TcpServerHandler handler;
    protected HashMap<UdpClientInfo, BasePlayerClient> connectedUdpClients = new HashMap<>();

    protected BaseServerConfig config;

    public BaseServer(BaseServerConfig config) {
        this.config = config;
    }

    @Override
    protected void onStart() {
        super.onStart();

        TickerPool.init(config.getTickGroupSize(), config.useHiresTimer());

        try {
            if (!config.getServerIP().equals(""))
                udpServerSocket = new DatagramSocket(config.getServerPort(), InetAddress.getByName(config.getServerIP()));
            else
                udpServerSocket = new DatagramSocket(config.getServerPort());
        } catch (IOException e) {
            e.printStackTrace();
        }

        udpThread = new Thread(UDP_SERVER_RUNNABLE);
        udpThread.start();

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
            b.bind(config.getServerPort() + 1).sync().channel().closeFuture().addListener(new GenericProgressiveFutureListener<ProgressiveFuture<Void>>() {
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

    @Override
    protected void onStop() {
        super.onStop();

        udpThread.interrupt();
    }

    public void disconnect(BasePlayerClient client) throws IOException {
        handler._close(client);
        onDisconnect(client);
    }

    void onDisconnect(BasePlayerClient client) throws IOException {
        System.out.println("[SERVER] " + client.getIpAddress() + " disconnected..");

        UdpClientInfo info = new UdpClientInfo(client.getIpAddress(), client.getPort());
        if (connectedUdpClients.containsKey(info))
            connectedUdpClients.remove(info);

        handler._disconnect(client);
    }

    public int getClientCount() {
        return handler.getClientCount();
    }

    public void sendUdpPacket(DatagramPacket packet) throws IOException {
        udpServerSocket.send(packet);
    }

    protected BasePlayerClient createClient() throws IOException {
        return new BasePlayerClient(this);
    }

    private void validateUdpSession(DatagramPacket packet) throws IOException {
        byte[] data = packet.getData();
        if (data[0] != 0x00)
            return;
        String session = new String(data, 1, 36, Charset.forName("ASCII"));
        Player player = PlayerFactory.getCreator().findPlayerByUUID(session);
        if (player == null || player.getClient() == null || player.getClient().isLoggedIn())
            return;

        UdpClientInfo info = new UdpClientInfo(packet.getAddress(), packet.getPort());
        connectedUdpClients.put(info, player.getClient());
        player.getClient().setIpAddress(packet.getAddress());
        player.getClient().setPort(packet.getPort());

        player.getClient().sendOk();
        log("UDP connection made with client " + info + " using session " + session);

        if (player.isInMatch() && !player.isSpectating()) {
            log("This playable was recently in a match....attempting to reconnect playable");
            ((NetworkMatch)player.getMatch()).addPlayer(player);
        } else if (player.isInMatch()) {
            log("This playable was recently spectating a match...attempting to reconnect playable");
            player.spectateConnect();
        }
    }

    private final Runnable UDP_SERVER_RUNNABLE = new Runnable() {
        @Override
        public void run() {
            Thread.currentThread().setName("UDP Server Listener");
            DatagramPacket recievePacket;
            byte[] receiveData;
            while (isRunning()) {
                try {
                    receiveData = new byte[1024];

                    recievePacket = new DatagramPacket(receiveData, 0, receiveData.length);
                    udpServerSocket.receive(recievePacket);

                    if (!isRunning())
                        break;

                    UdpClientInfo info = new UdpClientInfo(recievePacket.getAddress(), recievePacket.getPort());
                    BasePlayerClient client;
                    if ((client = connectedUdpClients.get(info)) != null) {
                        client.processUdpPacket(recievePacket);
                    } else {
                        new UdpAcceptThread(recievePacket).run();
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    private class UdpAcceptThread extends Thread {
        private DatagramPacket packet;
        public UdpAcceptThread(DatagramPacket packet) { this.packet = packet; }

        @Override
        public void run() {
            try {
                validateUdpSession(packet);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private class UdpClientInfo {
        private InetAddress address;
        private int port;

        public UdpClientInfo(InetAddress address, int port) {
            this.address = address;
            this.port = port;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            UdpClientInfo that = (UdpClientInfo) o;

            if (port != that.port) return false;
            if (!address.equals(that.address)) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = address.hashCode();
            result = 31 * result + port;
            return result;
        }

        @Override
        public String toString() {
            return "UdpClientInfo{" +
                    "address=" + address +
                    ", port=" + port +
                    '}';
        }
    }
}