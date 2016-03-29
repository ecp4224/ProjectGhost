package me.eddiep.ghost.game.match;

import me.eddiep.ghost.game.match.entities.PlayableEntity;
import me.eddiep.ghost.game.match.item.Item;
import me.eddiep.ghost.game.match.world.World;
import me.eddiep.ghost.game.match.stats.MatchHistory;
import me.eddiep.ghost.game.team.Team;
import me.eddiep.ghost.network.Server;
import me.eddiep.ghost.utils.Vector2f;

public interface LiveMatch extends Match {
    Team getTeam1();

    Team getTeam2();

    Team getWinningTeam();

    Team getLosingTeam();

    Team getTeamFor(PlayableEntity p);

    PlayableEntity[] getPlayers();

    Vector2f getLowerBounds();

    Vector2f getUpperBounds();

    long getTimeElapsed();

    World getWorld();

    void playableUpdated(PlayableEntity updated);

    void spawnItem(Item item);

    void despawnItem(Item item);

    boolean hasMatchStarted();

    boolean hasMatchEnded();

    MatchHistory matchHistory();

    void setup();

    void tick();

    Server getServer();

    boolean isMatchActive();

    void dispose();

    void onReady(PlayableEntity playableEntity);

    int getPlayerCount();

    void disableItems();

    void enableItems();
}
