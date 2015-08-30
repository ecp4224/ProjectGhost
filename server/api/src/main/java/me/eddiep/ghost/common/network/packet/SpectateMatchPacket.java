package me.eddiep.ghost.common.network.packet;

import me.eddiep.ghost.common.game.MatchFactory;
import me.eddiep.ghost.common.game.NetworkMatch;
import me.eddiep.ghost.common.network.BasePlayerClient;
import me.eddiep.ghost.common.network.BaseServer;
import me.eddiep.ghost.common.network.world.NetworkWorld;
import me.eddiep.ghost.game.match.Match;
import me.eddiep.ghost.network.packet.Packet;

import java.io.IOException;

public class SpectateMatchPacket extends Packet<BaseServer, BasePlayerClient> {
    public SpectateMatchPacket(BasePlayerClient client) {
        super(client);
    }

    @Override
    public void onHandlePacket(BasePlayerClient client)  throws IOException {
        long matchToSpectate = consume(8).asLong();

        Match m = MatchFactory.getCreator().findMatch(matchToSpectate);

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
