package me.eddiep.ghost.central.network.gameserv.actions;

import me.eddiep.ghost.central.network.gameserv.GameServerConnection;
import me.eddiep.ghost.central.network.gameserv.QueueInfo;

public class QueueInfoAction implements Action<QueueInfo> {

    @Override
    public String actionName() {
        return "queueInfoRequest";
    }

    @Override
    public Class<QueueInfo> dataClass() {
        return QueueInfo.class;
    }

    @Override
    public void performAction(GameServerConnection client, QueueInfo object) {
        client.getGameServer().setQueueInfo(object);
    }
}
