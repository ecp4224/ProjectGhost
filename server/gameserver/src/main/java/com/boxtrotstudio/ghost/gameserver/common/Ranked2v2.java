package com.boxtrotstudio.ghost.gameserver.common;

import com.boxtrotstudio.ghost.common.game.NetworkMatch;
import com.boxtrotstudio.ghost.game.match.entities.PlayableEntity;
import com.boxtrotstudio.ghost.game.queue.Queues;
import com.boxtrotstudio.ghost.gameserver.api.game.Game;
import com.boxtrotstudio.ghost.utils.Global;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Ranked2v2 implements Game {
    private List<String> maps = new ArrayList<>();

    @Override
    public Queues getQueue() {
        return Queues.RANKED_2V2;
    }

    @Override
    public void onServerStart() {
        File mapFolder = new File("maps");
        if (!mapFolder.exists())
            throw new RuntimeException("No maps found to load!");

        File[] maps = mapFolder.listFiles((dir, name) -> name.endsWith(".json") && !name.contains("tutorial"));

        for (File map : maps) {
            this.maps.add(map.getName().split("\\.")[0]);
        }
    }

    @Override
    public void onServerStop() { }

    @Override
    public short getPlayersPerMatch() {
        return 4;
    }

    @Override
    public void onMatchPreSetup(NetworkMatch activeMatch) { }

    @Override
    public void onMatchSetup(NetworkMatch activeMatch) {
        for (PlayableEntity p : activeMatch.getPlayers()) {
            p.setLives((byte) 3);
        }
    }

    @Override
    public String getMapName() {
        return maps.get(Global.RANDOM.nextInt(maps.size()));
    }
}
