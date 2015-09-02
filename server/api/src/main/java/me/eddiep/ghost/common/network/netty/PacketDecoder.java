package me.eddiep.ghost.common.network.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import me.eddiep.ghost.common.network.packet.PlayerPacketFactory;

import java.util.List;

public class PacketDecoder extends ByteToMessageDecoder {
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf byteBuf, List<Object> objects) throws Exception {
        if (byteBuf.readableBytes() == 0)
            return;

        int packetSize = PlayerPacketFactory.packetSize(byteBuf.getByte(0)) + 1;

        if (byteBuf.readableBytes() < packetSize)
            return;

        byte[] packet = new byte[packetSize];
        byteBuf.readBytes(packetSize).getBytes(0, packet);

        objects.add(packet);
    }
}
