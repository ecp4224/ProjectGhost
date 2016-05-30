package com.boxtrotstudio.ghost.gameserver.api.network.packets;

import com.boxtrotstudio.ghost.gameserver.api.network.MatchmakingClient;
import com.boxtrotstudio.ghost.network.packet.Packet;
import com.boxtrotstudio.ghost.common.network.BaseServer;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

public class MatchmakingPacketFactory {
    private static HashMap<Byte, Class<? extends Packet<BaseServer, MatchmakingClient>>> packets = new HashMap<>();

    static {
        packets.put((byte) 0x01, MatchmakingOkPacket.class);
        packets.put((byte) 0x25, CreateMatchPacket.class);
        packets.put((byte) 0x36, MatchmakingStreamUpdated.class);

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
