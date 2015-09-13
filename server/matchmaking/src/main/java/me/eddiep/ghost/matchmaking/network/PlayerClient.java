package me.eddiep.ghost.matchmaking.network;

import me.eddiep.ghost.matchmaking.network.database.Database;
import me.eddiep.ghost.matchmaking.network.packets.PacketFactory;
import me.eddiep.ghost.matchmaking.player.Player;
import me.eddiep.ghost.matchmaking.player.PlayerFactory;

import java.io.IOException;
import java.net.InetAddress;

public class PlayerClient extends TcpClient {
    private Player player;

    public PlayerClient(Player user, TcpServer server) throws IOException {
        super(server);

        this.player = user;

        this.player.setClient(this);
    }

    public Player getPlayer() {
        return player;
    }

    @Override
    public void handlePacket(byte[] rawData) throws IOException {
        byte opCode = rawData[0];
        byte[] data = new byte[rawData.length - 1];

        System.arraycopy(rawData, 1, data, 0, data.length);

        PacketFactory.getPlayerPacket(opCode, PlayerClient.this, data).handlePacket().endTCP();
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
        }

        PlayerFactory.invalidateSession(player);
        Database.saveRank(player);

        player = null;
    }
}
