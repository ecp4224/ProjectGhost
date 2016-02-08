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
        PACKETS[0x07] = MatchEndPacket.class;
        //DEPRECATED 0X09 UDPPING
        PACKETS[0x10] = SpawnEntityPacket.class;
        PACKETS[0x11] = DespawnEntityPacket.class;
        PACKETS[0x12] = PlayerStatePacket.class;
        //DEPRECATED 0X19 PING
        PACKETS[0x30] = SpawnEffectPacket.class;
        PACKETS[0x31] = StatsUpdatePacket.class;
        PACKETS[0x32] = ItemActivatedPacket.class;
        PACKETS[0x33] = ItemDeactivatedPacket.class;
        PACKETS[0x35] = MapSettingsPacket.class;
        PACKETS[0x37] = SpawnLightPacket.class;
        PACKETS[0x38] = UpdateInventoryPacket.class;
        PACKETS[0x40] = EventPacket.class;
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
