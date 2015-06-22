package me.eddiep.ghost.gameserver.packets;

import me.eddiep.ghost.gameserver.ActiveMatch;
import me.eddiep.ghost.gameserver.TcpUdpClient;
import me.eddiep.ghost.gameserver.TcpUdpServer;
import me.eddiep.ghost.network.packet.Packet;
import me.eddiep.ghost.game.Match;
import me.eddiep.ghost.gameserver.MatchFactory;

import java.io.IOException;

public class SpectateMatchPacket extends Packet<TcpUdpServer, TcpUdpClient> {
    public SpectateMatchPacket(TcpUdpClient client) {
        super(client);
    }

    @Override
    public void onHandlePacket(TcpUdpClient client)  throws IOException {
        long matchToSpectate = consume(8).asLong();

        Match m = MatchFactory.findMatch(matchToSpectate);

        if (m instanceof ActiveMatch) {
            ActiveMatch activeMatch = (ActiveMatch)m;

            if (!activeMatch.hasMatchEnded()) {
                activeMatch.addSpectator(client);
                client.sendOk(true);
            } else {
                client.sendOk(false);
            }
        } else {
            client.sendOk(false);
        }
    }
}
