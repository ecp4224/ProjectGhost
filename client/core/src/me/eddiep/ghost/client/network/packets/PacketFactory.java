package me.eddiep.ghost.client.network.packets;

import me.eddiep.ghost.client.network.Packet;
import me.eddiep.ghost.client.network.PlayerClient;

public class PacketFactory {
    private static final Class<? extends Packet<? extends PlayerClient>>[] PACKETS = new Class[255];

    static {
        PACKETS[0x01] = OKPacket.class;
        PACKETS[0x02] = MatchInfoPacket.class;
        PACKETS[0x04] = BulkEntityStatePacket.class;
        PACKETS[0x06] = MatchStatusPacket.class;
        //TODO 0X07 MATCHEND
        PACKETS[0x10] = SpawnEntityPacket.class;
        PACKETS[0x11] = DespawnEntityPacket.class;
        PACKETS[0x12] = PlayerStatePacket.class;
        PACKETS[0x31] = StatsUpdatePacket.class;
        PACKETS[0x35] = MapSettingsPacket.class;
    }

    public static Packet<PlayerClient> getPacket(int opCode) {
        Class class_ = PACKETS[opCode];

        if (class_ == null) {
            System.err.println("Invalid opcode: " + opCode);
        }

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
