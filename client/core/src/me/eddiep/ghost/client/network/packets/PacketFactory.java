package me.eddiep.ghost.client.network.packets;

import me.eddiep.ghost.client.network.Packet;
import me.eddiep.ghost.client.network.PlayerClient;

public class PacketFactory {
    private static final Class<? extends Packet<? extends PlayerClient>>[] PACKETS = new Class[255];

    static {
        PACKETS[0x01] = OKPacket.class;
        PACKETS[0x02] = MatchInfoPacket.class;
    }

    public static Packet<PlayerClient> getPacket(int opCode) {
        Class class_ = PACKETS[opCode];

        try {
            return (Packet<PlayerClient>) class_.newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static Packet<PlayerClient> getPacket(int opCode, byte[] data) {
        Packet<PlayerClient> p = getPacket(opCode);
        if (p != null)
            p.attachPacket(data);

        return p;
    }
}
