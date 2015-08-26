package me.eddiep.ghost.gameserver.api.network.packets;

import me.eddiep.ghost.network.packet.Packet;
import me.eddiep.ghost.gameserver.api.network.TcpUdpClient;
import me.eddiep.ghost.gameserver.api.network.TcpUdpServer;

import java.io.IOException;

public class ActionRequestPacket extends Packet<TcpUdpServer, TcpUdpClient> {
    public ActionRequestPacket(TcpUdpClient client, byte[] data) {
        super(client, data);
    }

    @Override
    public void onHandlePacket(TcpUdpClient client)  throws IOException {
        int packetNumber = consume(4).asInt();
        if (packetNumber < client.getLastReadPacket()) {
            int dif = client.getLastReadPacket() - packetNumber;
            if (dif >= Integer.MAX_VALUE - 1000) {
                client.setLastReadPacket(packetNumber);
            } else return;
        } else {
            client.setLastReadPacket(packetNumber);
        }

        byte actionType = consume(1).asByte();
        float mouseX = consume(4).asFloat();
        float mouseY = consume(4).asFloat();
        //long time = consume(4).asLong();

        if (actionType == 0)
            client.getPlayer().moveTowards(mouseX, mouseY);
        else if (actionType >= 1)
            client.getPlayer().fireTowards(mouseX, mouseY, actionType);
        else
            System.err.println("[SERVER] Unknown action " + actionType + " ! (" + client.getIpAddress() + ")");
    }
}
