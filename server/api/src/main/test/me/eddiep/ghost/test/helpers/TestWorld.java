package me.eddiep.ghost.test.helpers;

import me.eddiep.ghost.game.match.LiveMatch;
import me.eddiep.ghost.game.match.world.WorldImpl;
import me.eddiep.ghost.game.match.world.timeline.WorldSnapshot;
import me.eddiep.ghost.network.Client;

import java.io.IOException;

public class TestWorld extends WorldImpl {
    public TestWorld(LiveMatch match) {
        super(match);
    }

    @Override
    public String mapName() {
        return "NA";
    }

    @Override
    protected void onTimelineTick() { }

    @Override
    public void requestEntityUpdate() { }

    @Override
    public void updateClient(Client client) throws IOException { }

    @Override
    public void updateClient(Client client, WorldSnapshot snapshot) throws IOException { }
}
