package me.eddiep.ghost.gameserver.api.network.packets;

import me.eddiep.ghost.gameserver.api.network.MatchmakingClient;
import me.eddiep.ghost.gameserver.api.network.TcpUdpServer;
import me.eddiep.ghost.network.packet.Packet;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

public class MatchmakingPacketFactory {
    private static HashMap<Byte, Class<? extends Packet<TcpUdpServer, MatchmakingClient>>> packets = new HashMap<>();

    static {
        packets.put((byte) 0x01, MatchmakingOkPacket.class);
    }

    public static Packet get(byte opCode, MatchmakingClient client) {
        try {
            return packets.get(opCode).getConstructor(MatchmakingClient.class).newInstance(client);
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

    public static Packet get(byte opCode, MatchmakingClient client, byte[] data) {
        try {
            return packets.get(opCode).getConstructor(MatchmakingClient.class, byte[].class).newInstance(client, data);
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
