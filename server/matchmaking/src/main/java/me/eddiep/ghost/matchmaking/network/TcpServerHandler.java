package me.eddiep.ghost.matchmaking.network;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import me.eddiep.ghost.matchmaking.Main;
import me.eddiep.ghost.matchmaking.network.gameserver.Stream;
import me.eddiep.ghost.matchmaking.network.packets.GameServerVerificationPacket;
import me.eddiep.ghost.matchmaking.network.packets.UpdateSessionPacket;
import me.eddiep.ghost.matchmaking.player.Player;
import me.eddiep.ghost.matchmaking.player.PlayerFactory;
import me.eddiep.ghost.network.sql.PlayerData;

import java.io.IOException;
import java.net.InetSocketAddress;
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
                PlayerClient pclient = new PlayerClient(server);
                InetSocketAddress socketAddress = (InetSocketAddress)channelHandlerContext.channel().remoteAddress();
                pclient.attachChannel(channelHandlerContext);
                clients.put(channelHandlerContext, pclient);

                String session = new String(data, 1, 60, Charset.forName("ASCII"));
                byte streamType = data[61];
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
                pclient.attachPlayer(player, socketAddress.getAddress());
                pclient.sendOk();

                server.connectedClients.add(pclient);

                UpdateSessionPacket packet = new UpdateSessionPacket(pclient);
                packet.writePacket();

                System.out.println("TCP connection made with client " + socketAddress.getAddress() + " using session " + session);
            } else if (data[0] == 0x23) {
                GameServerClient tempClient = new GameServerClient(server);
                tempClient.attachChannel(channelHandlerContext);

                byte[] newData = new byte[data.length - 1];

                System.arraycopy(data, 1, newData, 0, newData.length);

                GameServerVerificationPacket packet = new GameServerVerificationPacket(tempClient, newData);
                packet.handlePacket().endTCP();

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
        System.out.println("Client connected @ " + ctx.channel().remoteAddress());
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

    void _disconnect(TcpClient client) throws IOException {
        clients.remove(client.getChannel());
        client.disconnect();
    }

    private void _disconnect(ChannelHandlerContext ctx) throws IOException {
        TcpClient c;
        if ((c = clients.get(ctx)) != null) {
            c.disconnect();
            clients.remove(ctx);
        }
        ctx.close();
    }
}
