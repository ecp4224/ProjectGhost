package me.eddiep.ghost.test.network.packet;

import me.eddiep.ghost.game.match.LiveMatch;
import me.eddiep.ghost.game.match.entities.PlayableEntity;
import me.eddiep.ghost.game.match.world.timeline.EntitySnapshot;
import me.eddiep.ghost.game.match.world.timeline.WorldSnapshot;
import me.eddiep.ghost.network.packet.Packet;
import me.eddiep.ghost.test.game.Player;
import me.eddiep.ghost.test.network.TcpUdpClient;
import me.eddiep.ghost.test.network.TcpUdpServer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BulkEntityStatePacket extends Packet<TcpUdpServer, TcpUdpClient> {
    public BulkEntityStatePacket(TcpUdpClient client) {
        super(client);
    }

    @Override
    protected void onWritePacket(TcpUdpClient client, Object... args) throws IOException {
        if (args.length == 0)
            return;

        WorldSnapshot snapshot = (WorldSnapshot) args[0];
        LiveMatch match = (LiveMatch)args[1];
        //List<Entity> toUpdate = (List<Entity>) args[0];

        int lastWrite = client.getLastWritePacket() + 1;
        client.setLastWritePacket(lastWrite);

        List<EntitySnapshot> snapshots = calculateSendArray(client, snapshot.getEntitySnapshots(), match);

        write((byte)0x04);
        write(lastWrite);
        write(snapshots.size()); //Amount of entities in this bulk

        for (EntitySnapshot entity : snapshots) {
            writeEntity(client, entity, match);
        }

        client.getServer().sendUdpPacket(
                endUDP()
        );
    }

    private List<EntitySnapshot> calculateSendArray(TcpUdpClient client, EntitySnapshot[] array, LiveMatch match) {
        if (client.getPlayer().isSpectating()) {
            System.out.println("Sending " + array.length + " entities!");
            return Arrays.asList(array);
        }

        ArrayList<EntitySnapshot> snapshots = new ArrayList<>();

        for (EntitySnapshot entity : array) {
            Player p = client.getPlayer();
            if (entity.isPlayer() && p.isInMatch() && !p.getTeam().isAlly(entity)) {
                PlayableEntity p1 = match.getWorld().getEntity(entity.getID());
                if (!p1.shouldSendUpdatesTo(p))
                    continue;
            }

            snapshots.add(entity);
        }

        return snapshots;
    }

    private void writeEntity(TcpUdpClient client, EntitySnapshot entity, LiveMatch match) throws IOException {
        short id = client.getPlayer().getID() == entity.getID() ? 0 : entity.getID();
        int iAlpha = entity.getAlpha();
        if (id == 0) {
            if (iAlpha < 150)
                iAlpha = 150;
        }

        //boolean isVisible = entity.equals(client.getPlayer()) || entity.isVisible();
        boolean isPlayer = entity.isPlayer();
        boolean hasTarget = isPlayer && entity.getTarget() != null;
        if (client.getPlayer().isSpectating() || client.getPlayer().getTeam().isAlly(entity)) { //Allies are always visible
            if (iAlpha < 150)
                iAlpha = 150;
        }

        //byte alpha = (byte)iAlpha; java bytes can suck my dick
        write(id)
                .write(entity.getPosition().x)
                .write(entity.getPosition().y)
                .write(entity.getVelocity().x)
                .write(entity.getVelocity().y)
                .write(iAlpha)
                .write(entity.getRotation())
                .write(match.getTimeElapsed())
                .write(hasTarget);

        if (hasTarget) {

            write(entity.getTarget().x)
                    .write(entity.getTarget().y);
        }

        /*if (isPlayer && id == 0) {
            BaseNetworkPlayer p = (BaseNetworkPlayer) entity;

            write(p.getVisibleIndicatorPosition());
        }*/
    }
}
