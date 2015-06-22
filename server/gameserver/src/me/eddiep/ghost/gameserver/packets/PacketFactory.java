package me.eddiep.ghost.gameserver.packets;


import me.eddiep.ghost.gameserver.TcpUdpClient;
import me.eddiep.ghost.gameserver.TcpUdpServer;
import me.eddiep.ghost.network.packet.Packet;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

public class PacketFactory {
    private static HashMap<Byte, Class<? extends Packet<TcpUdpServer, TcpUdpClient>>> packets = new HashMap<>();

    static {
        packets.put((byte) 0x01, OkPacket.class);
        packets.put((byte) 0x02, MatchFoundPacket.class);
        packets.put((byte) 0x03, ReadyPacket.class);
        packets.put((byte) 0x04, EntityStatePacket.class);
        packets.put((byte) 0x08, ActionRequestPacket.class);
        packets.put((byte) 0x09, PingPongPacket.class);
        packets.put((byte) 0x10, SpawnEntityPacket.class);
        packets.put((byte) 0x11, DespawnEntityPacket.class);
        packets.put((byte) 0x12, PlayerStatePacket.class);
        packets.put((byte) 0x22, ChangeAbilityPacket.class);
    }

    public static Packet get(byte opCode, TcpUdpClient client) {
        try {
            return packets.get(opCode).getConstructor(TcpUdpClient.class).newInstance(client);
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

    public static Packet get(byte opCode, TcpUdpClient client, byte[] data) {
        try {
            return packets.get(opCode).getConstructor(TcpUdpClient.class, byte[].class).newInstance(client, data);
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
