package me.eddiep.ghost.gameserver.api.network.world;

import me.eddiep.ghost.game.match.world.WorldImpl;
import me.eddiep.ghost.game.match.world.timeline.WorldSnapshot;
import me.eddiep.ghost.gameserver.api.network.ActiveMatch;
import me.eddiep.ghost.gameserver.api.network.TcpUdpClient;
import me.eddiep.ghost.gameserver.api.network.User;
import me.eddiep.ghost.gameserver.api.network.packets.BulkEntityStatePacket;
import me.eddiep.ghost.network.Client;

import java.io.IOException;
import java.util.ArrayList;

public class NetworkWorld extends WorldImpl {
    private ActiveMatch match;
    private ArrayList<User> connectedPlayers = new ArrayList<User>();
    private ArrayList<User> connectedSpectators = new ArrayList<>();

    public NetworkWorld(ActiveMatch match) {
        super(match);
        this.match = match;
    }

    public ActiveMatch getActiveMatch() {
        return match;
    }

    @Override
    public void requestEntityUpdate() {
        for (User p : connectedPlayers) {
            super.presentCursor.sendClientSnapshot(p.getClient());
        }

        for (User s : connectedSpectators) {
            super.spectatorCursor.sendClientSnapshot(s.getClient());
        }
    }

    @Override
    public void updateClient(Client client) throws IOException {
        WorldSnapshot snapshot = super.presentCursor.get();

        updateClient(client, snapshot);
    }

    @Override
    public void updateClient(Client client, WorldSnapshot snapshot) throws IOException {
        if (!(client instanceof TcpUdpClient))
            return;

        TcpUdpClient c = (TcpUdpClient)client;

        BulkEntityStatePacket packet = new BulkEntityStatePacket(c);
        packet.writePacket(snapshot);
    }
}
