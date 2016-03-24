package me.eddiep.ghost.matchmaking.network.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import me.eddiep.ghost.matchmaking.network.packets.PacketFactory;

import java.nio.ByteOrder;
import java.util.List;

public class PacketDecoder extends ByteToMessageDecoder {
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf byteBuf, List<Object> objects) throws Exception {
        if (byteBuf.readableBytes() == 0)
            return;

        byte opCode = byteBuf.getByte(0);
        int packetSize = PacketFactory.packetSize(opCode) + 1;

        if (opCode == -1) {
            System.err.println("Unknown op code: " + opCode);
        }
        if (packetSize < 0) { //Size is first 4 bytes of packet
            packetSize = byteBuf.order(ByteOrder.LITTLE_ENDIAN).getInt(1); //Size should be after the opCode
        }

        if (byteBuf.readableBytes() < packetSize)
            return;

        byte[] packet = new byte[packetSize];
        byteBuf.readBytes(packetSize).getBytes(0, packet);

        objects.add(packet);
    }
}
