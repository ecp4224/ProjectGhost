package com.boxtrotstudio.ghost.matchmaking.network.packets;

import com.boxtrotstudio.ghost.matchmaking.network.GameServerClient;
import com.boxtrotstudio.ghost.matchmaking.player.Player;
import com.boxtrotstudio.ghost.network.packet.Packet;
import com.boxtrotstudio.ghost.network.sql.PlayerData;
import com.boxtrotstudio.ghost.game.queue.Queues;
import com.boxtrotstudio.ghost.matchmaking.network.TcpServer;

import java.io.IOException;

public class CreateMatchPacket extends Packet<TcpServer, GameServerClient> {
    public CreateMatchPacket(GameServerClient client) {
        super(client);
    }

    @Override
    public void onWritePacket(GameServerClient client, Object... args) throws IOException {
        Queues queues = (Queues)args[0];
        long mId = (long)args[1];
        Player[] team1 = (Player[])args[2];
        Player[] team2 = (Player[])args[3];

        byte team1Length = (byte) team1.length;
        byte team2Length = (byte) team2.length;

        write((byte)0x25)
                .write(queues.asByte())
                .write(mId)
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
        private byte weapon;

        public PlayerPacketObject(Player p) {
            this.session = p.getSession();
            this.stats = p.getStats();
            this.weapon = p.getCurrentAbility().id();
        }
    }
}
