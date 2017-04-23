package com.boxtrotstudio.ghost.common.network.packet;

import com.boxtrotstudio.ghost.common.network.BasePlayerClient;
import com.boxtrotstudio.ghost.common.network.BaseServer;
import com.boxtrotstudio.ghost.network.packet.Packet;

import java.util.HashMap;

public class PlayerPacketFactory {
    private static HashMap<Byte, Packet<BaseServer, ? extends BasePlayerClient>> packets = new HashMap<>();
    //private static HashMap<Byte, Class<? extends Packet<BaseServer, ? extends BasePlayerClient>>> packets = new HashMap<>();
    private static HashMap<Byte, Integer> packetSize = new HashMap<>();

    static {
        packetSize.put((byte) 0x00, 37); //Session packet

        //packets.put((byte) 0x01, OkPacket.class); //server -> client
        //packets.put((byte) 0x02, MatchFoundPacket.class); //server -> client
        packets.put((byte) 0x03, new ReadyPacket()); //client -> server
        packetSize.put((byte) 0x03, 1);
        //packets.put((byte) 0x04, EntityStatePacket.class);
        //packets.put((byte) 0x05, QueueRequestPacket.class);
        packets.put((byte) 0x08, new ActionRequestPacket()); //client -> server [UDP]
        packets.put((byte) 0x09, new PingPongPacket()); //client -> server, server -> client
        packetSize.put((byte) 0x08, 13);
        packetSize.put((byte) 0x09, 4);
        //packets.put((byte) 0x10, SpawnEntityPacket.class); //server -> client
        //packets.put((byte) 0x11, DespawnEntityPacket.class); //server -> client
        //packets.put((byte) 0x12, PlayerStatePacket.class); //server -> client
        //TODO Packet 0x13 - ???
        packets.put((byte) 0x14, new SetDisplayNamePacket());
        //packets.put((byte) 0x15, NewNotificationPacket.class); //server -> client
        //packets.put((byte) 0x16, DeleteRequestPacket.class); //server -> client
        packets.put((byte) 0x17, new RespondRequestPacket()); //client -> server
        packetSize.put((byte) 0x17, 5);
        //TODO Packet 0x18 - PrivateMatchReady Packet
        packetSize.put((byte) 0x19, 4);
        //packets.put((byte) 0x20, LeaveQueuePacket.class);
        packets.put((byte) 0x22, new ChangeAbilityPacket()); //client -> server
        packetSize.put((byte) 0x22, 1);

        packets.put((byte) 0x28, new SpectateMatchPacket()); //client -> server
        packetSize.put((byte) 0x28, 8);

        packets.put((byte)0x39, new UseItemRequest()); //client -> server [UDP]

        packets.put((byte)0x41, new SetNamePacket());
        packetSize.put((byte)0x41, 255);
    }

    public static int packetSize(byte opCode) {
        if (!packetSize.containsKey(opCode))
            return -1;
        return packetSize.get(opCode);
    }

    public static void addPacket(byte opCode, int size, Packet<BaseServer, BasePlayerClient> packet) {
        packets.put(opCode, packet);
        packetSize.put(opCode, size);
    }

    public static Packet get(byte opCode) {
        if (!packets.containsKey(opCode))
            return null;
        return packets.get(opCode);
    }
}
