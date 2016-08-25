package com.boxtrotstudio.ghost.matchmaking.network.packets;

import com.boxtrotstudio.ghost.matchmaking.network.GameServerClient;
import com.boxtrotstudio.ghost.matchmaking.network.PlayerClient;
import com.boxtrotstudio.ghost.matchmaking.network.TcpClient;
import com.boxtrotstudio.ghost.matchmaking.network.TcpServer;
import com.boxtrotstudio.ghost.network.packet.Packet;

import java.util.HashMap;

public class PacketFactory {
    private static HashMap<Byte, Packet<TcpServer, ? extends TcpClient>> packets = new HashMap<>();
    private static HashMap<Byte, Integer> sizes = new HashMap<>();

    static {
        sizes.put((byte) 0x00, 61); //Session packet


        packets.put((byte) 0x05, new QueueRequestPacket()); //client -> server
        sizes.put((byte) 0x05, 1);

        packets.put((byte) 0x17, new RespondRequestPacket()); //client -> server
        sizes.put((byte) 0x17, 5);

        //TODO Packet 0x18 - PrivateMatchReady Packet
        packets.put((byte) 0x20, new LeaveQueuePacket()); //client -> server
        sizes.put((byte)0x20, 0);

        packets.put((byte) 0x22, new ChangeAbilityPacket()); //client -> server
        sizes.put((byte) 0x22, 1);

        packets.put((byte) 0x23, new GameServerVerificationPacket()); //client -> server
        sizes.put((byte) 0x23, 40);

        packets.put((byte) 0x24, new GameServerInfoPacket()); //client -> server
        sizes.put((byte) 0x24, 13);

        packets.put((byte) 0x27, new MatchHistoryPacket()); //client -> server
        sizes.put((byte)0x27, -2); //Size is in packet

        sizes.put((byte) 0x34, 32); //Admin verify packet

        packets.put((byte)0x41, new SetNamePacket());
        sizes.put((byte)0x41, 255);

        packets.put((byte)0x90, new GameServerOkPacket());
        sizes.put((byte)0x90, 1);
    }

    public static int packetSize(byte opCode) {
        if (!sizes.containsKey(opCode))
            return -1;
        return sizes.get(opCode);
    }

    public static Packet<TcpServer, PlayerClient> getPlayerPacket(byte opCode) {
        if (packets.containsKey(opCode))
            return (Packet<TcpServer, PlayerClient>) packets.get(opCode);
        return null;
    }

    public static Packet<TcpServer, GameServerClient> getGameServerPacket(byte opCode) {
        if (packets.containsKey(opCode))
            return (Packet<TcpServer, GameServerClient>) packets.get(opCode);
        return null;
    }
}
