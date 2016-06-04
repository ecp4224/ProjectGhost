package com.boxtrotstudio.ghost.matchmaking.network;

import com.boxtrotstudio.ghost.matchmaking.network.packets.PacketFactory;
import com.boxtrotstudio.ghost.matchmaking.player.Player;
import com.boxtrotstudio.ghost.matchmaking.player.PlayerFactory;
import com.boxtrotstudio.ghost.network.packet.Packet;
import com.boxtrotstudio.ghost.matchmaking.network.database.Database;

import java.io.IOException;
import java.net.InetAddress;

public class PlayerClient extends TcpClient {
    private Player player;

    public PlayerClient(TcpServer server) throws IOException {
        super(server);
    }

    public Player getPlayer() {
        return player;
    }

    @Override
    public void handlePacket(byte[] rawData) throws IOException {
        byte opCode = rawData[0];
        byte[] data = new byte[rawData.length - 1];

        System.arraycopy(rawData, 1, data, 0, data.length);

        Packet packet = PacketFactory.getPlayerPacket(opCode, this, data);
        if (packet == null)
            throw new IllegalAccessError("Invalid opcode sent!");

        packet.handlePacket();
        packet.endTCP();
    }


    public void attachPlayer(Player player, InetAddress address) {
        this.player = player;
        this.player.setClient(this);

        this.IpAddress = address;
    }

    @Override
    public void onDisconnect() throws IOException {
        super.onDisconnect();

        if (player != null) {
            if (player.isInQueue()) {
                player.getQueue().removeUserFromQueue(player);
            }

            player.disconnected();

            PlayerFactory.invalidateSession(player);
        }

        if (Database.isSetup()) {
            Database.saveRank(player.getRanking());
        }

        player = null;
    }
}
