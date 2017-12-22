package com.boxtrotstudio.ghost.common.network.packet;

import com.boxtrotstudio.ghost.common.game.Player;
import com.boxtrotstudio.ghost.common.network.BasePlayerClient;
import com.boxtrotstudio.ghost.common.network.BaseServer;
import com.boxtrotstudio.ghost.game.match.world.World;
import com.boxtrotstudio.ghost.network.packet.Packet;
import com.boxtrotstudio.ghost.utils.Vector2f;

import java.io.IOException;

public class ActionRequestPacket extends Packet<BaseServer, BasePlayerClient> {

    @Override
    public void onHandlePacket(BasePlayerClient client)  throws IOException {
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
            client.getPlayer().fireTowards(mouseX, mouseY, false);
        else if (actionType == 1)
            client.getPlayer().fireTowards(mouseX, mouseY, true);
        else if (actionType == 2) {
            Vector2f direction = new Vector2f(mouseX, mouseY);

            if (direction.length() != 0f)
                direction.normalise();

            client.getPlayer().moveWithDirection(direction);
        }
        else if (actionType == 3) { //Pathfinding
            Player player = client.getPlayer();
            World world = player.getWorld();
            Vector2f target = new Vector2f(mouseX, mouseY);


            boolean shouldPathfind = world.getPhysics().projectLine(player.getPosition(), target);
            if (!shouldPathfind) {
                player.moveTowards(mouseX, mouseY);
            } else {
                //TODO Do pathfinding
                player.moveTowards(mouseX, mouseY); //DELETE THIS WHEN COMPLETE
            }
        }
        else
            System.err.println("[SERVER] Unknown action " + actionType + " ! (" + client.getIpAddress() + ')');
    }
}
