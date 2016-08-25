package com.boxtrotstudio.ghost.gameserver.api.network.packets;

import com.boxtrotstudio.ghost.common.network.BaseServer;
import com.boxtrotstudio.ghost.gameserver.api.network.MatchmakingClient;
import com.boxtrotstudio.ghost.network.packet.Packet;

import java.util.HashMap;

public class MatchmakingPacketFactory {
    private static HashMap<Byte, Packet<BaseServer, MatchmakingClient>> packets = new HashMap<>();

    static {
        packets.put((byte) 0x01, new MatchmakingOkPacket());
        packets.put((byte) 0x25, new CreateMatchPacket());
        packets.put((byte) 0x36, new MatchmakingStreamUpdated());

    }

    public static Packet<BaseServer, MatchmakingClient> get(byte opCode) {
        if (packets.containsKey(opCode))
            return packets.get(opCode);
        return null;
    }
}
