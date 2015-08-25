package me.eddiep.ghost.gameserver.api.network.packets;

import me.eddiep.ghost.game.match.Match;
import me.eddiep.ghost.gameserver.api.network.MatchFactory;
import me.eddiep.ghost.gameserver.api.network.NetworkMatch;
import me.eddiep.ghost.gameserver.api.network.TcpUdpClient;
import me.eddiep.ghost.gameserver.api.network.TcpUdpServer;
import me.eddiep.ghost.gameserver.api.network.world.NetworkWorld;
import me.eddiep.ghost.network.packet.Packet;

import java.io.IOException;

public class SpectateMatchPacket extends Packet<TcpUdpServer, TcpUdpClient> {
    public SpectateMatchPacket(TcpUdpClient client) {
        super(client);
    }

    @Override
    public void onHandlePacket(TcpUdpClient client)  throws IOException {
        long matchToSpectate = consume(8).asLong();

        Match m = MatchFactory.INSTANCE.findMatch(matchToSpectate);

        if (m instanceof NetworkMatch) {
            NetworkMatch activeMatch = (NetworkMatch)m;

            if (!activeMatch.hasMatchEnded()) {
               client.getPlayer().spectateMatch(activeMatch);
                client.sendOk(true);

                if (client.getPlayer().isUDPConnected()) {
                    ((NetworkWorld)activeMatch.getWorld()).addSpectator(client.getPlayer());
                }
            } else {
                client.sendOk(false);
            }
        } else {
            client.sendOk(false);
        }
    }
}
