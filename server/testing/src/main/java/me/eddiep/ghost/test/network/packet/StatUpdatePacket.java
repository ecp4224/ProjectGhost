package me.eddiep.ghost.test.network.packet;

import me.eddiep.ghost.game.match.stats.Stat;
import me.eddiep.ghost.network.packet.Packet;
import me.eddiep.ghost.test.network.TcpUdpClient;
import me.eddiep.ghost.test.network.TcpUdpServer;

import java.io.IOException;

public class StatUpdatePacket extends Packet<TcpUdpServer, TcpUdpClient> {

    public StatUpdatePacket(TcpUdpClient client) {
        super(client);
    }

    @Override
    protected void onWritePacket(TcpUdpClient client, Object... args) throws IOException {
        if (args.length != 1 || !(args[0] instanceof Stat)) {
            return;
        }

        Stat stat = (Stat) args[0];
        String id = stat.getId();
        double value = stat.getValue();

        if (id.length() > 4) {
            throw new IllegalArgumentException("Status string should not be more than 4 bytes long.");
        } else if (id.length() < 4) {
            id = id + "    ".substring(id.length());
        }

        write((byte) 0x31)
                .write(id)
                .write(value)
                .endTCP();
    }
}
