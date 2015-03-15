package me.eddiep.ghost.server.network.packet.impl;

import me.eddiep.ghost.server.game.Entity;
import me.eddiep.ghost.server.game.entities.Player;
import me.eddiep.ghost.server.network.Client;
import me.eddiep.ghost.server.network.packet.Packet;

import java.io.IOException;

public class EntityStatePacket extends Packet {
    public EntityStatePacket(Client client) {
        super(client);
    }

    @Override
    protected void onWritePacket(Client client, Object... args) throws IOException {
        if (args.length != 1)
            return;

        Entity entity = (Entity)args[0];
        short id = client.getPlayer().equals(entity) ? 0 : entity.getID();
        boolean isVisible = entity.equals(client.getPlayer()) || entity.isVisible();
        boolean hasTarget = entity instanceof Player && ((Player)entity).getTarget() != null;
        int lastWrite = client.getLastWritePacket() + 1;
        client.setLastWritePacket(lastWrite);

        write((byte)0x04)
                .write(lastWrite)
                .write(id)
                .write(entity.getPosition().x)
                .write(entity.getPosition().y)
                .write(entity.getVelocity().x)
                .write(entity.getVelocity().y)
                .write(isVisible)
                .write(entity.getMatch().getTimeElapsed())
                .write(hasTarget);

        if (hasTarget) {
            Player p = (Player)entity;

            write(p.getTarget().x)
           .write(p.getTarget().y);
        }

        client.getServer().sendUdpPacket(
                endUDP()
        );
    }
}
