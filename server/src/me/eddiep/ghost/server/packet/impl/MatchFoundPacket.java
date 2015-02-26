package me.eddiep.ghost.server.packet.impl;

import me.eddiep.ghost.server.network.Client;
import me.eddiep.ghost.server.packet.Packet;

import java.io.IOException;

public class MatchFoundPacket extends Packet {
    public MatchFoundPacket(Client client) {
        super(client);
    }

    @Override
    protected void onWritePacket(Client client, Object... args) throws IOException{
        if (args.length != 5)
            return;

        Client playingAgainst = (Client)args[0];
        short startX = (short)args[1];
        short startY = (short)args[2];
        short opStartX = (short)args[3];
        short opStartY = (short)args[4];

        write((byte)0x02)
                .write((byte)playingAgainst.getPlayer().getUsername().length())
                .write(playingAgainst.getPlayer().getUsername())
                .write(startX)
                .write(startY)
                .write(opStartX)
                .write(opStartY)
                .endTCP();
    }
}
