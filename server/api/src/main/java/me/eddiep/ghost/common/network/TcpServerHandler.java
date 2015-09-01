package me.eddiep.ghost.common.network;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import me.eddiep.ghost.common.game.Player;
import me.eddiep.ghost.common.game.PlayerFactory;
import me.eddiep.ghost.network.Client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.util.HashMap;

public class TcpServerHandler extends SimpleChannelInboundHandler<Byte[]> {
    private BaseServer server;
    private HashMap<ChannelHandlerContext, BasePlayerClient> clients = new HashMap<>();

    public TcpServerHandler(BaseServer server) {
        this.server = server;
    }

    public BaseServer getServer() {
        return server;
    }

    @Override
    protected void messageReceived(ChannelHandlerContext channelHandlerContext, Byte[] bytes) throws Exception {
        byte[] data = toPrimitives(bytes);
        BasePlayerClient client = clients.get(channelHandlerContext);
        if (client == null) {
            _disconnect(channelHandlerContext);
        } else {
            if (client.getPlayer() != null) {
                client.handlePacket(data);
            } else { //Expect session packet
                if (bytes[0] != 0x00) {
                    _disconnect(channelHandlerContext);
                    return;
                }

                String session = new String(data, 1, 36, Charset.forName("ASCII"));
                final Player player = PlayerFactory.getCreator().findPlayerByUUID(session);
                if (player == null) {
                    _disconnect(channelHandlerContext);
                    return;
                }

                InetSocketAddress socketAddress = (InetSocketAddress)channelHandlerContext.channel().remoteAddress();
                client.attachPlayer(player, socketAddress.getAddress());
                client.attachChannel(channelHandlerContext);
                client.sendOk();
            }
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        BasePlayerClient client = server.createClient();
        clients.put(ctx, client);
    }

    private void _disconnect(ChannelHandlerContext ctx) throws IOException {
        clients.get(ctx).disconnect();
        clients.remove(ctx);
        ctx.close();
    }

    private byte[] toPrimitives(Byte[] oBytes) {
        byte[] bytes = new byte[oBytes.length];

        for (int i = 0; i < oBytes.length; i++) {
            bytes[i] = oBytes[i];
        }

        return bytes;
    }
}
