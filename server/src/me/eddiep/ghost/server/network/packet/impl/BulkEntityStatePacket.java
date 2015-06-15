package me.eddiep.ghost.server.network.packet.impl;

import me.eddiep.ghost.server.game.Entity;
import me.eddiep.ghost.server.game.entities.playable.impl.Player;
import me.eddiep.ghost.server.network.Client;
import me.eddiep.ghost.server.network.packet.Packet;

import java.io.IOException;

public class BulkEntityStatePacket extends Packet {
    public BulkEntityStatePacket(Client client) {
        super(client);
    }

    @Override
    protected void onWritePacket(Client client, Object... args) throws IOException {
        if (args.length == 0)
            return;

        int lastWrite = client.getLastWritePacket() + 1;
        client.setLastWritePacket(lastWrite);

        write((byte)0x04);
        write(lastWrite);
        write(args.length); //Amount of entities in this bulk

        for (Object obj : args) {
            Entity entity = (Entity)obj;
            writeEntity(client, entity);
        }

        client.getServer().sendUdpPacket(
                endUDP()
        );
    }

    private void writeEntity(Client client, Entity entity) throws IOException {
        short id = client.getPlayer().equals(entity) ? 0 : entity.getID();
        int iAlpha = entity.getAlpha();
        if (entity.equals(client.getPlayer())) {
            if (iAlpha < 150)
                iAlpha = 150;
        }

        //boolean isVisible = entity.equals(client.getPlayer()) || entity.isVisible();
        boolean isPlayer = entity instanceof Player;
        boolean hasTarget = isPlayer && ((Player)entity).getTarget() != null;

        if (entity instanceof Player) {
            if (client.getPlayer().getTeam().isAlly((Player)entity)) { //Allies are always visible
                if (iAlpha < 150)
                    iAlpha = 150;
            }
        }

        //byte alpha = (byte)iAlpha; java bytes can suck my dick
         write(id)
                .write(entity.getPosition().x)
                .write(entity.getPosition().y)
                .write(entity.getVelocity().x)
                .write(entity.getVelocity().y)
                .write(iAlpha)
                .write(entity.getRotation())
                .write(entity.getMatch().getTimeElapsed())
                .write(hasTarget);

        if (hasTarget) {
            Player p = (Player)entity;

            write(p.getTarget().x)
                    .write(p.getTarget().y);
        }

        if (isPlayer && id == 0) {
            Player p = (Player)entity;

            write(p.getVisibleIndicatorPosition());
        }
    }
}
