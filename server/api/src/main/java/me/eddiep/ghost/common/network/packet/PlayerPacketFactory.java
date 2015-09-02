package me.eddiep.ghost.common.network.packet;

import me.eddiep.ghost.common.network.BasePlayerClient;
import me.eddiep.ghost.common.network.BaseServer;
import me.eddiep.ghost.network.packet.Packet;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

public class PlayerPacketFactory {
    private static HashMap<Byte, Class<? extends Packet<BaseServer, ? extends BasePlayerClient>>> packets = new HashMap<>();
    private static HashMap<Byte, Integer> packetSize = new HashMap<>();

    static {
        packetSize.put((byte) 0x00, 36); //Session packet

        packets.put((byte) 0x01, OkPacket.class); //server -> client
        packets.put((byte) 0x02, MatchFoundPacket.class); //server -> client
        packets.put((byte) 0x03, ReadyPacket.class); //client -> server
        packetSize.put((byte) 0x03, 1);
        //packets.put((byte) 0x04, EntityStatePacket.class);
        //packets.put((byte) 0x05, QueueRequestPacket.class);
        packets.put((byte) 0x08, ActionRequestPacket.class); //client -> server [UDP]
        packets.put((byte) 0x09, PingPongPacket.class); //client -> server, server -> client
        packetSize.put((byte) 0x09, 4);
        packets.put((byte) 0x10, SpawnEntityPacket.class); //server -> client
        packets.put((byte) 0x11, DespawnEntityPacket.class); //server -> client
        packets.put((byte) 0x12, PlayerStatePacket.class); //server -> client
        //TODO Packet 0x13 - ???
        packets.put((byte) 0x14, SetDisplayNamePacket.class);
        packets.put((byte) 0x15, NewNotificationPacket.class); //server -> client
        packets.put((byte) 0x16, DeleteRequestPacket.class); //server -> client
        packets.put((byte) 0x17, RespondRequestPacket.class); //client -> server
        packetSize.put((byte) 0x17, 5);
        //TODO Packet 0x18 - PrivateMatchReady Packet
        packets.put((byte) 0x19, TcpPingPongPacket.class); //client -> server
        packetSize.put((byte) 0x19, 4);
        //packets.put((byte) 0x20, LeaveQueuePacket.class);
        packets.put((byte) 0x22, ChangeAbilityPacket.class); //client -> server
        packetSize.put((byte) 0x22, 1);

        packets.put((byte) 0x28, SpectateMatchPacket.class); //client -> server
        packetSize.put((byte) 0x28, 8);
    }

    public static int packetSize(byte opCode) {
        return packetSize.get(opCode);
    }

    public static void addPacket(byte opCode, Class<? extends Packet<BaseServer, BasePlayerClient>> packet) {
        packets.put(opCode, packet);
    }

    public static Packet get(byte opCode, BasePlayerClient client) {
        try {
            return packets.get(opCode).getConstructor(BasePlayerClient.class).newInstance(client);
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

    public static Packet get(byte opCode, BasePlayerClient client, byte[] data) {
        try {
            return packets.get(opCode).getConstructor(BasePlayerClient.class, byte[].class).newInstance(client, data);
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
