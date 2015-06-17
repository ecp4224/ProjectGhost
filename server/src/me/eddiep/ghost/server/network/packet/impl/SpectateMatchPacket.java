package me.eddiep.ghost.server.network.packet.impl;

import me.eddiep.ghost.server.game.ActiveMatch;
import me.eddiep.ghost.server.game.Match;
import me.eddiep.ghost.server.game.MatchFactory;
import me.eddiep.ghost.server.network.Client;
import me.eddiep.ghost.server.network.packet.Packet;

import java.io.IOException;

public class SpectateMatchPacket extends Packet {
    public SpectateMatchPacket(Client client) {
        super(client);
    }

    @Override
    public void onHandlePacket(Client client)  throws IOException {
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
