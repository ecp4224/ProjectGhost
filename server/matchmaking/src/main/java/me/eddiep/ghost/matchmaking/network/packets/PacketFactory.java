package me.eddiep.ghost.matchmaking.network.packets;

import me.eddiep.ghost.matchmaking.network.GameServerClient;
import me.eddiep.ghost.matchmaking.network.PlayerClient;
import me.eddiep.ghost.matchmaking.network.TcpClient;
import me.eddiep.ghost.matchmaking.network.TcpServer;
import me.eddiep.ghost.network.packet.Packet;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

public class PacketFactory {
    private static HashMap<Byte, Class<? extends Packet<TcpServer, ? extends TcpClient>>> packets = new HashMap<>();
    private static HashMap<Byte, Integer> sizes = new HashMap<>();

    static {
        sizes.put((byte) 0x00, 62); //Session packet

        packets.put((byte) 0x01, OkPacket.class); //server -> client
        packets.put((byte) 0x02, MatchFoundPacket.class); //server -> client

        packets.put((byte) 0x05, QueueRequestPacket.class); //client -> server
        sizes.put((byte) 0x05, 1);

        //TODO Packet 0x13 - ???
        //packets.put((byte) 0x14, SetDisplayNamePacket.class);
        packets.put((byte) 0x15, NewNotificationPacket.class); //server -> client
        packets.put((byte) 0x16, DeleteRequestPacket.class); //server -> client

        packets.put((byte) 0x17, RespondRequestPacket.class); //client -> server
        sizes.put((byte) 0x17, 5);

        //TODO Packet 0x18 - PrivateMatchReady Packet
        packets.put((byte) 0x20, LeaveQueuePacket.class); //client -> server
        sizes.put((byte)0x20, 1);

        packets.put((byte) 0x22, ChangeAbilityPacket.class); //client -> server
        sizes.put((byte) 0x22, 1);

        packets.put((byte) 0x23, GameServerVerificationPacket.class); //client -> server
        sizes.put((byte) 0x23, 40);

        packets.put((byte) 0x24, GameServerInfoPacket.class); //client -> server
        sizes.put((byte) 0x24, 13);

        packets.put((byte) 0x25, CreateMatchPacket.class); //server -> client
        packets.put((byte) 0x26, MatchRedirectPacket.class); //server -> client

        packets.put((byte) 0x27, MatchHistoryPacket.class); //client -> server
        sizes.put((byte)0x27, -2); //Size is in packet

        sizes.put((byte) 0x34, 32); //Admin verify packet

        packets.put((byte)0x41, SetNamePacket.class);
        sizes.put((byte)0x41, 255);

        packets.put((byte)0x90, GameServerOkPacket.class);
        sizes.put((byte)0x90, 1);
    }

    public static int packetSize(byte opCode) {
        if (!sizes.containsKey(opCode))
            return -1;
        return sizes.get(opCode);
    }

    public static Packet getPlayerPacket(byte opCode, PlayerClient client, byte[] data) {
        try {
            return packets.get(opCode).getConstructor(PlayerClient.class, byte[].class).newInstance(client, data);
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Packet getGameServerPacket(byte opCode, GameServerClient client, byte[] data) {
        try {
            return packets.get(opCode).getConstructor(GameServerClient.class, byte[].class).newInstance(client, data);
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        return null;
    }
}
