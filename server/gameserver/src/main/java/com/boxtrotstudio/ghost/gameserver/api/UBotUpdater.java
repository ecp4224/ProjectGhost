package com.boxtrotstudio.ghost.gameserver.api;

import com.boxtrotstudio.ghost.common.game.MatchFactory;
import me.eddiep.ubot.UBot;
import me.eddiep.ubot.module.UpdateScheduler;
import me.eddiep.ubot.utils.Schedule;
import me.eddiep.ubot.utils.UpdateType;

import java.io.IOException;

public class UBotUpdater implements UpdateScheduler {
    @Override
    public void onPreCheck(UBot uBot) { }

    @Override
    public Schedule<UpdateType> shouldBuild(UpdateType updateType, UBot uBot) {
        return Schedule.now(); //Always build new updates
    }

    @Override
    public Schedule<UpdateType> shouldPatch(UpdateType updateType, UBot uBot) {
        GameServer.currentStream = Stream.BUFFERED;

        System.err.println(updateType.name() + " update found!");
        System.err.println("This server has been moved to the buffered stream!");
        if (MatchFactory.getCreator().getAllActiveMatches().size() > 0) {
            System.err.println("This server will shutdown when all games finish..");
        }

        return Schedule.when(() -> MatchFactory.getCreator().getAllActiveMatches().size() == 0);
    }

    @Override
    public void patchComplete(UpdateType updateType, UBot uBot) {
        if (MatchFactory.getCreator().getAllActiveMatches().size() > 0) {
            GameServer.getServer().getLogger().error("Ubot tried to update while server is live!");
            GameServer.currentStream = Stream.BUFFERED;
            GameServer.getServer().getLogger().error("Server will restart when all matches end..");
            return;
        }

        try {
            GameServer.restartServer();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void init() { }

    @Override
    public void dispose() { }
}
