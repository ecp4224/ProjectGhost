package com.boxtrotstudio.ghost.common.network.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import com.boxtrotstudio.ghost.common.network.packet.PlayerPacketFactory;

import java.util.List;

public class PacketDecoder extends ByteToMessageDecoder {
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf byteBuf, List<Object> objects) throws Exception {
        if (byteBuf.readableBytes() == 0)
            return;

        byte opCode = byteBuf.getByte(0);
        int packetSize = PlayerPacketFactory.packetSize(opCode) + 1;

        if (opCode == -1) {
            System.err.println("Unknown op code: " + opCode);
        }

        if (byteBuf.readableBytes() < packetSize)
            return;

        byte[] packet = new byte[packetSize];
        byteBuf.readBytes(packetSize).getBytes(0, packet);

        objects.add(packet);
    }
}
