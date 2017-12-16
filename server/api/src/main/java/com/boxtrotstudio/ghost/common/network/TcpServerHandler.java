package com.boxtrotstudio.ghost.common.network;

import com.boxtrotstudio.ghost.common.game.Player;
import com.boxtrotstudio.ghost.common.game.PlayerFactory;
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
    private BaseServer server;
    private HashMap<ChannelHandlerContext, BasePlayerClient> clients = new HashMap<>();

    public TcpServerHandler(BaseServer server) {
        this.server = server;
    }

    public BaseServer getServer() {
        return server;
    }

    @Override
    protected void messageReceived(ChannelHandlerContext channelHandlerContext, byte[] data) throws Exception {
        BasePlayerClient client = clients.get(channelHandlerContext);
        if (client == null) {
            _disconnect(channelHandlerContext);
        } else {
            if (client.getPlayer() != null) {
                client.handlePacket(data);
            } else { //Expect session packet
                if (data[0] != 0x00) {
                    _disconnect(channelHandlerContext);
                    return;
                }

                ByteBuffer buf = ByteBuffer.allocate(data.length).order(ByteOrder.LITTLE_ENDIAN).put(data);
                buf.position(0);
                byte opcode = buf.get();
                short length = buf.getShort();
                String session = new String(data, 3, length, Charset.forName("ASCII"));
                final Player player = PlayerFactory.getCreator().findPlayerByUUID(session);
                if (player == null) {
                    _disconnect(channelHandlerContext);
                    return;
                }

                InetSocketAddress socketAddress = (InetSocketAddress)channelHandlerContext.channel().remoteAddress();
                client.attachPlayer(player, socketAddress.getAddress());
                client.attachChannel(channelHandlerContext);
                client.sendOk();

                server.getLogger().info("TCP connection made with client " + socketAddress.getAddress() + " using session " + session);
            }
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        BasePlayerClient client = server.createClient();
        clients.put(ctx, client);
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

    void _close(BasePlayerClient client) throws IOException {
        client.getChannel().close();
    }

    void _disconnect(BasePlayerClient client) throws IOException {

        clients.remove(client.getChannel());
        client.disconnect();
    }

    public void _disconnect(ChannelHandlerContext ctx) throws IOException {
        BasePlayerClient b = clients.get(ctx);
        if (b != null)
            b.disconnect();

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

    public int getClientCount() {
        return clients.size();
    }
}
