package me.eddiep.ghost.gameserver.api.network.packets;

import me.eddiep.ghost.game.match.entities.Entity;
import me.eddiep.ghost.gameserver.api.game.player.Player;
import me.eddiep.ghost.gameserver.api.network.TcpUdpClient;
import me.eddiep.ghost.gameserver.api.network.TcpUdpServer;
import me.eddiep.ghost.network.packet.Packet;

import java.io.IOException;

public class EntityStatePacket extends Packet<TcpUdpServer, TcpUdpClient> {
    public EntityStatePacket(TcpUdpClient client) {
        super(client);
    }

    @Override
    protected void onWritePacket(TcpUdpClient client, Object... args) throws IOException {
        if (args.length != 1)
            return;

        Entity entity = (Entity)args[0];
        short id = client.getPlayer().equals(entity) ? 0 : entity.getID();
        int iAlpha = entity.getAlpha();
        if (entity.equals(client.getPlayer())) {
            if (iAlpha < 150)
                iAlpha = 150;
        }

        //boolean isVisible = entity.equals(client.getPlayer()) || entity.isVisible();
        boolean isPlayer = entity instanceof Player;
        boolean hasTarget = isPlayer && ((Player)entity).getTarget() != null;
        int lastWrite = client.getLastWritePacket() + 1;
        client.setLastWritePacket(lastWrite);

        if (entity instanceof Player) {
            if (client.getPlayer().getTeam().isAlly((Player)entity)) { //Allies are always visible
                if (iAlpha < 150)
                    iAlpha = 150;
            }
        }

        //byte alpha = (byte)iAlpha; java bytes can suck my dick

        write((byte)0x04)
                .write( lastWrite)
                .write(1) //We are only updating 1 entity
                .write(id)
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

        client.getServer().sendUdpPacket(
                endUDP()
        );
    }
}
