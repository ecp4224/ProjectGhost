package me.eddiep.ghost.common.network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import me.eddiep.ghost.common.network.packet.PlayerPacketFactory;

import java.util.List;

public class PacketDecoder extends ByteToMessageDecoder {
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf byteBuf, List<Object> objects) throws Exception {
        int packetSize = PlayerPacketFactory.packetSize(byteBuf.getByte(0));

        if (byteBuf.readableBytes() < packetSize)
            return;

        objects.add(byteBuf.readBytes(packetSize));
    }
}
