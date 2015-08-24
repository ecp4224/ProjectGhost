package me.eddiep.ghost.test.helpers;

import me.eddiep.ghost.game.match.LiveMatchImpl;
import me.eddiep.ghost.game.match.entities.PlayableEntity;
import me.eddiep.ghost.game.match.world.World;
import me.eddiep.ghost.game.queue.Queues;
import me.eddiep.ghost.game.team.Team;
import me.eddiep.ghost.network.Server;
import me.eddiep.ghost.utils.Vector2f;

public class TestMatch extends LiveMatchImpl {

    public static final int MAP_XMIN = 0;
    public static final int MAP_XMAX = 1024;
    public static final int MAP_XMIDDLE = MAP_XMIN + ((MAP_XMAX - MAP_XMIN) / 2);
    public static final int MAP_YMIN = 0;
    public static final int MAP_YMAX = 720;
    public static final int MAP_YMIDDLE = MAP_YMIN + ((MAP_YMAX - MAP_YMIN) / 2);

    public static final Vector2f LOWER_BOUNDS = new Vector2f(MAP_XMIN, MAP_YMIN);
    public static final Vector2f UPPER_BOUNDS = new Vector2f(MAP_XMAX, MAP_YMAX);

    public TestMatch(Team team1, Team team2, Server server) {
        super(team1, team2, server);
    }

    void setWorld(World world) {
        super.world = world;
    }

    @Override
    protected void onSetup() {
        super.queue = Queues.ORIGINAL;
    }

    @Override
    protected void onPlayerAdded(PlayableEntity playableEntity) {

    }

    @Override
    protected void onMatchEnded() {

    }

    @Override
    public Vector2f getLowerBounds() {
        return LOWER_BOUNDS;
    }

    @Override
    public Vector2f getUpperBounds() {
        return UPPER_BOUNDS;
    }

    public String getStatusMessage() {
        return super.lastActiveReason;
    }
}
