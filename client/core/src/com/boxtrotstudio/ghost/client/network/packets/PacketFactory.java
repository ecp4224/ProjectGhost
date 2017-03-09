package com.boxtrotstudio.ghost.client.network.packets;

import com.boxtrotstudio.ghost.client.network.Packet;
import com.boxtrotstudio.ghost.client.network.PlayerClient;

public class PacketFactory {
    private static final Packet<PlayerClient>[] PACKETS = new Packet[255];

    static {
        PACKETS[0x01] = new OKPacket();
        PACKETS[0x02] = new MatchInfoPacket();
        PACKETS[0x04] = new BulkEntityStatePacket();
        PACKETS[0x06] = new MatchStatusPacket();
        PACKETS[0x07] = new MatchEndPacket();
        //DEPRECATED 0X09 UDPPING
        PACKETS[0x10] = new SpawnEntityPacket();
        PACKETS[0x11] = new DespawnEntityPacket();
        PACKETS[0x12] = new PlayerStatePacket();
        //DEPRECATED 0X19 PING
        PACKETS[0x26] = new MatchRedirectPacket();
        PACKETS[0x29] = new UpdateSessionPacket();
        PACKETS[0x30] = new SpawnEffectPacket();
        PACKETS[0x31] = new StatsUpdatePacket();
        PACKETS[0x32] = new ItemActivatedPacket();
        PACKETS[0x33] = new ItemDeactivatedPacket();
        PACKETS[0x35] = new MapSettingsPacket();
        PACKETS[0x37] = new SpawnLightPacket();
        PACKETS[0x38] = new UpdateInventoryPacket();
        PACKETS[0x40] = new EventPacket();
        PACKETS[0x42] = new DisconnectReason();
        PACKETS[0x43] = new DisplayTextPacket();
        PACKETS[0x44] = new RemoveTextPacket();
    }

    public static Packet<PlayerClient> getPacket(int opCode) {
        Packet<PlayerClient> packet = PACKETS[opCode];

        if (packet == null) {
            System.err.println("Invalid opcode: " + opCode);
            return null;
        }

        return packet;
    }

    public static Packet<PlayerClient> getPacket(int opCode, byte[] data) {
        Packet<PlayerClient> p = getPacket(opCode);
        if (p != null)
            p.attachPacket(data);

        return p;
    }
}
