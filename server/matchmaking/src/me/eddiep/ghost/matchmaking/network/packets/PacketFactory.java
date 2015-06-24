package me.eddiep.ghost.matchmaking.network.packets;

import me.eddiep.ghost.matchmaking.network.GameServerClient;
import me.eddiep.ghost.matchmaking.network.PlayerClient;
import me.eddiep.ghost.matchmaking.network.TcpClient;
import me.eddiep.ghost.matchmaking.network.TcpServer;
import me.eddiep.ghost.network.Server;
import me.eddiep.ghost.network.packet.Packet;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

public class PacketFactory {
    private static HashMap<Byte, Class<? extends Packet<TcpServer, ? extends TcpClient>>> packets = new HashMap<>();

    static {
        packets.put((byte) 0x01, OkPacket.class);
        packets.put((byte) 0x02, MatchFoundPacket.class);
        packets.put((byte) 0x05, QueueRequestPacket.class);
        //TODO Packet 0x13 - ???
        packets.put((byte) 0x14, SetDisplayNamePacket.class);
        packets.put((byte) 0x15, NewNotificationPacket.class);
        packets.put((byte) 0x16, DeleteRequestPacket.class);
        packets.put((byte) 0x17, RespondRequestPacket.class);
        //TODO Packet 0x18 - PrivateMatchReady Packet
        packets.put((byte) 0x20, LeaveQueuePacket.class);
    }

    public static <T extends Server> Packet getPlayerPacket(byte opCode, PlayerClient client) {
        try {
            return packets.get(opCode).getConstructor(PlayerClient.class).newInstance(client);
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

    public static <T extends Server> Packet getGameServerPacket(byte opCode, GameServerClient client) {
        try {
            return packets.get(opCode).getConstructor(GameServerClient.class).newInstance(client);
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
