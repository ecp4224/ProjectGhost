package me.eddiep.ghost.test.network.packet;

import me.eddiep.ghost.game.match.Match;
import me.eddiep.ghost.network.packet.Packet;
import me.eddiep.ghost.test.game.MatchFactory;
import me.eddiep.ghost.test.game.NetworkMatch;
import me.eddiep.ghost.test.network.TcpUdpClient;
import me.eddiep.ghost.test.network.TcpUdpServer;
import me.eddiep.ghost.test.network.world.NetworkWorld;

import java.io.IOException;

public class SpectateMatchPacket extends Packet<TcpUdpServer, TcpUdpClient> {
    public SpectateMatchPacket(TcpUdpClient client) {
        super(client);
    }

    @Override
    public void onHandlePacket(TcpUdpClient client)  throws IOException {
        long matchToSpectate = consume(8).asLong();

        Match m = MatchFactory.findMatch(matchToSpectate);

        if (m instanceof NetworkMatch) {
            NetworkMatch activeMatch = (NetworkMatch)m;

            if (!activeMatch.hasMatchEnded()) {
                ((NetworkWorld)activeMatch.getWorld()).addSpectator(client.getPlayer());
                client.sendOk(true);
            } else {
                client.sendOk(false);
            }
        } else {
            client.sendOk(false);
        }
    }
}
