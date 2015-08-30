package me.eddiep.ghost.matchmaking.network;

import me.eddiep.ghost.matchmaking.network.database.Database;
import me.eddiep.ghost.matchmaking.network.packets.PacketFactory;
import me.eddiep.ghost.matchmaking.player.Player;
import me.eddiep.ghost.matchmaking.player.PlayerFactory;

import java.io.IOException;
import java.net.Socket;

public class PlayerClient extends TcpClient {
    private Player player;

    public PlayerClient(Player user, Socket socket, TcpServer server) throws IOException {
        super(socket, server);

        this.player = user;

        this.player.setClient(this);
    }

    public Player getPlayer() {
        return player;
    }

    @Override
    public void handlePacket(byte opCode) throws IOException {
        PacketFactory.getPlayerPacket(opCode, PlayerClient.this).handlePacket().endTCP();
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
