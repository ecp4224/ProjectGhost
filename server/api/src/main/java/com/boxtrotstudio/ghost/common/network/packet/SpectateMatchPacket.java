package com.boxtrotstudio.ghost.common.network.packet;

import com.boxtrotstudio.ghost.common.game.MatchFactory;
import com.boxtrotstudio.ghost.common.game.NetworkMatch;
import com.boxtrotstudio.ghost.common.network.BasePlayerClient;
import com.boxtrotstudio.ghost.common.network.BaseServer;
import com.boxtrotstudio.ghost.common.network.world.NetworkWorld;
import com.boxtrotstudio.ghost.game.match.Match;
import com.boxtrotstudio.ghost.network.packet.Packet;

import java.io.IOException;

public class SpectateMatchPacket extends Packet<BaseServer, BasePlayerClient> {

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
