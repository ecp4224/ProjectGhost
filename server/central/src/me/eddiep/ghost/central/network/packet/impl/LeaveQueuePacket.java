package me.eddiep.ghost.central.network.packet.impl;

import me.eddiep.ghost.central.network.Client;

import java.io.IOException;

public class LeaveQueuePacket extends GameServerPacket {
    public LeaveQueuePacket(Client client) {
        super(client);
    }

    @Override
    public void onWritePacket(Client client, Object... args) throws IOException {
        super.onWritePacket(client, args); //Validate we are sending to a gameserver

        String session = (String)args[0];

        write((byte)0x20)
                .write(session.length())
                .write(session)
                .endTCP();
    }

    @Override
    public void onHandlePacket(Client client) throws IOException {
        byte type = consume(1).asByte();

        /*if (!PlayerFactory.checkSession(client.getPlayer().getSession().toString())) {
            OkPacket packet = new OkPacket(client);
            packet.writePacket(false);

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            client.getServer().disconnect(client);
            return;
        }

        if (client.getPlayer().isInMatch()) {
            OkPacket packet = new OkPacket(client);
            packet.writePacket(false);
        } else if (client.getPlayer().getQueue().queue().asByte() != type) {
            OkPacket packet = new OkPacket(client);
            packet.writePacket(false);
        } else {
            client.getPlayer().getQueue().removeUserFromQueue(client.getPlayer());
            OkPacket packet = new OkPacket(client);
            packet.writePacket(true);
        }*/
    }
}
