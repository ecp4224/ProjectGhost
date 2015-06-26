package me.eddiep.ghost.matchmaking.network.packets;

import me.eddiep.ghost.matchmaking.network.GameServerClient;
import me.eddiep.ghost.matchmaking.network.TcpServer;
import me.eddiep.ghost.matchmaking.player.Player;
import me.eddiep.ghost.network.packet.Packet;
import me.eddiep.ghost.network.sql.PlayerData;

import java.io.IOException;

public class CreateMatchPacket extends Packet<TcpServer, GameServerClient> {
    public CreateMatchPacket(GameServerClient client) {
        super(client);
    }

    @Override
    public void onWritePacket(GameServerClient client, Object... args) throws IOException {
        Player[] team1 = (Player[])args[0];
        Player[] team2 = (Player[])args[1];

        byte team1Length = (byte) team1.length;
        byte team2Length = (byte) team2.length;

        write((byte)0x25)
                .write(team1Length)
                .write(team2Length);

        for (Player p : team1) {
            PlayerPacketObject obj = new PlayerPacketObject(p);
            write(obj);
        }

        for (Player p : team2) {
            PlayerPacketObject obj = new PlayerPacketObject(p);
            write(obj);
        }

        endTCP();
    }

    private class PlayerPacketObject {
        private String session;
        private PlayerData stats;

        public PlayerPacketObject(Player p) {
            this.session = p.getSession();
            this.stats = p.getStats();
        }
    }
}
