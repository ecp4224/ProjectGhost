package com.boxtrotstudio.ghost.matchmaking.network;

import com.boxtrotstudio.ghost.matchmaking.Main;
import com.boxtrotstudio.ghost.matchmaking.core.hosts.gameserver.Stream;
import com.boxtrotstudio.ghost.matchmaking.network.packets.GameServerVerificationPacket;
import com.boxtrotstudio.ghost.matchmaking.network.packets.UpdateSessionPacket;
import com.boxtrotstudio.ghost.matchmaking.player.Player;
import com.boxtrotstudio.ghost.matchmaking.player.PlayerFactory;
import com.boxtrotstudio.ghost.network.sql.PlayerData;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.util.HashMap;

@ChannelHandler.Sharable
public class TcpServerHandler extends SimpleChannelInboundHandler<byte[]> {
    private TcpServer server;
    private HashMap<ChannelHandlerContext, TcpClient> clients = new HashMap<>();

    public TcpServerHandler(TcpServer server) {
        this.server = server;
    }

    public TcpServer getServer() {
        return server;
    }

    @Override
    protected void messageReceived(ChannelHandlerContext channelHandlerContext, byte[] data) throws Exception {
        TcpClient client = clients.get(channelHandlerContext);
        if (client == null) {
            if (data[0] == 0x00) {
                PlayerClient pClient = new PlayerClient(server);
                InetSocketAddress socketAddress = (InetSocketAddress)channelHandlerContext.channel().remoteAddress();
                pClient.attachChannel(channelHandlerContext);
                clients.put(channelHandlerContext, pClient);

                ByteBuffer buf = ByteBuffer.allocate(data.length).order(ByteOrder.LITTLE_ENDIAN).put(data);
                buf.position(0);

                byte opcode = buf.get();
                short length = buf.getShort();
                String session = new String(data, 3, length, Charset.forName("ASCII"));
                byte streamType = data[data.length - 1];

                PlayerData pdata = Main.SESSION_VALIDATOR.validate(session);
                if (pdata == null) {
                    _disconnect(channelHandlerContext);
                    return;
                }

                Stream defaultStream = Stream.fromInt(server.getConfig().defaultStream());
                Stream highestAllowedStream = Stream.fromInt(pdata.getStreamPermission());
                Stream requestedStream = Stream.fromInt(streamType);
                if (!requestedStream.allowed(highestAllowedStream, defaultStream)) {
                    _disconnect(channelHandlerContext);
                    return;
                }

                final Player player = PlayerFactory.registerPlayer(pdata.getUsername(), pdata, requestedStream);
                pClient.attachPlayer(player, socketAddress.getAddress());
                pClient.sendOk();

                server.connectedClients.add(pClient);

                UpdateSessionPacket packet = new UpdateSessionPacket(pClient);
                packet.writePacket();

                server.getLogger().info("TCP connection made with client " + socketAddress.getAddress() + " using session " + session);
            } else if (data[0] == 0x23) {
                GameServerClient tempClient = new GameServerClient(server);
                tempClient.attachChannel(channelHandlerContext);

                byte[] newData = new byte[data.length - 1];

                System.arraycopy(data, 1, newData, 0, newData.length);

                GameServerVerificationPacket packet = new GameServerVerificationPacket();
                packet.handlePacket(tempClient, newData).endTCP();

                if (tempClient.isConnected()) {
                    clients.put(channelHandlerContext, tempClient);
                }
            } else {
                _disconnect(channelHandlerContext);
            }
        } else {
            client.handlePacket(data);
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        server.getLogger().info("Client connected @ " + ctx.channel().remoteAddress());
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

    void _onDisconnect(TcpClient client) throws IOException {
        clients.remove(client.getChannel());
        client.disconnect();
    }

    void _disconnect(TcpClient client) throws IOException {
        client.getChannel().close();
        _onDisconnect(client);
    }

    void _disconnect(ChannelHandlerContext ctx) throws IOException {
        TcpClient c;
        if ((c = clients.get(ctx)) != null) {
            c.disconnect();
            clients.remove(ctx);
        }
        ctx.close();
    }
}
